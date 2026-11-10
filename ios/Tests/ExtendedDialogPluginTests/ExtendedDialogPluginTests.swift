import XCTest
import Capacitor
@testable import ExtendedDialogPlugin

@MainActor
class ExtendedDialogTests: XCTestCase {
    func testRapidReplacementsCancelSupersededCalls() async {
        let plugin = DeferredDismissalPlugin()
        var results: [String: [Bool]] = [:]
        var presented: [String] = []
        var callbacks: [String: ([String: Any]) -> Void] = [:]
        let first = makeCall("first") { results["first", default: []].append($0) }
        let second = makeCall("second") { results["second", default: []].append($0) }
        let third = makeCall("third") { results["third", default: []].append($0) }

        plugin.presentDialog(first, cancelResult: ["value": false]) { done in
            presented.append("first")
            callbacks["first"] = done
        }
        await flushMainQueue()
        plugin.finishDismissals()

        for call in [second, third] {
            plugin.presentDialog(call, cancelResult: ["value": false]) { done in
                presented.append(call.callbackId)
                callbacks[call.callbackId] = done
            }
        }
        await flushMainQueue()

        XCTAssertEqual(results["first"], [false])
        XCTAssertEqual(results["second"], [false])
        XCTAssertNil(results["third"])

        plugin.finishDismissals()
        XCTAssertEqual(presented, ["first", "third"])
        callbacks["third"]?(["value": true])
        callbacks["first"]?(["value": true])

        XCTAssertEqual(results["first"], [false])
        XCTAssertEqual(results["second"], [false])
        XCTAssertEqual(results["third"], [true])
    }

    func testLateResponseFromReplacedCallIsIgnored() async {
        let plugin = DeferredDismissalPlugin()
        var results: [String: [Bool]] = [:]
        var firstDone: (([String: Any]) -> Void)?
        var secondDone: (([String: Any]) -> Void)?
        let first = makeCall("first") { results["first", default: []].append($0) }
        let second = makeCall("second") { results["second", default: []].append($0) }

        plugin.presentDialog(first, cancelResult: ["value": false]) { firstDone = $0 }
        await flushMainQueue()
        plugin.finishDismissals()
        plugin.presentDialog(second, cancelResult: ["value": false]) { secondDone = $0 }
        await flushMainQueue()
        plugin.finishDismissals()

        firstDone?(["value": true])
        XCTAssertEqual(results["first"], [false])
        XCTAssertNil(results["second"])
        secondDone?(["value": true])
        XCTAssertEqual(results["second"], [true])
    }

    func testTimeoutCancelsActiveCall() async {
        let plugin = DeferredDismissalPlugin()
        let cancelled = expectation(description: "Active call resolves as cancelled")
        let call = makeCall("timeout", timeout: 10) { value in
            XCTAssertFalse(value)
            cancelled.fulfill()
        }

        plugin.presentDialog(call, cancelResult: ["value": false]) { _ in }
        await flushMainQueue()
        plugin.finishDismissals()

        await fulfillment(of: [cancelled], timeout: 1)
        plugin.finishDismissals()
    }

    func testDismissLoadingSettlesAndNextDialogWaitsForClose() async {
        let plugin = DeferredDismissalPlugin()
        var loadingResults: [Bool] = []
        var sheetPresented = false
        let loading = makeLoadingCall("loading") { loadingResults.append($0) }

        plugin.presentDialog(loading, cancelResult: ["cancelled": false]) { _ in }
        await flushMainQueue()
        plugin.finishDismissals()

        plugin.dismissLoading(makeDismissCall())
        plugin.presentDialog(makeCall("sheet") { _ in }, cancelResult: ["value": false]) { _ in sheetPresented = true }
        await flushMainQueue()

        XCTAssertEqual(loadingResults, [false])
        XCTAssertFalse(sheetPresented, "Must wait for the loading sheet to finish closing")

        plugin.finishDismissals()
        XCTAssertTrue(sheetPresented)
    }

    func testDismissLoadingCancelsQueuedLoading() async {
        let plugin = DeferredDismissalPlugin()
        var results: [Bool] = []
        var loadingPresented = false
        let first = makeCall("first") { _ in }
        let loading = makeLoadingCall("loading") { results.append($0) }

        plugin.presentDialog(first, cancelResult: ["value": false]) { _ in }
        await flushMainQueue()
        plugin.finishDismissals()

        plugin.presentDialog(loading, cancelResult: ["cancelled": false]) { _ in loadingPresented = true }
        await flushMainQueue()
        plugin.dismissLoading(makeDismissCall())
        await flushMainQueue()
        plugin.finishDismissals()

        XCTAssertEqual(results, [false])
        XCTAssertFalse(loadingPresented)
    }

    func testDismissLoadingLeavesOtherDialogsOpen() async {
        let plugin = DeferredDismissalPlugin()
        var results: [Bool] = []
        let sheet = makeCall("sheet") { results.append($0) }

        plugin.presentDialog(sheet, cancelResult: ["value": false]) { _ in }
        await flushMainQueue()
        plugin.finishDismissals()

        plugin.dismissLoading(makeDismissCall())
        plugin.dismissLoading(makeDismissCall())
        await flushMainQueue()

        XCTAssertEqual(results, [])
        XCTAssertEqual(plugin.pendingDismissals, 0)
    }

    func testUserCancelReportsCancelledAndLateDismissIsNoOp() async {
        let plugin = DeferredDismissalPlugin()
        var results: [Bool] = []
        var done: (([String: Any]) -> Void)?
        let loading = makeLoadingCall("loading") { results.append($0) }

        plugin.presentDialog(loading, cancelResult: ["cancelled": false]) { done = $0 }
        await flushMainQueue()
        plugin.finishDismissals()

        done?(["cancelled": true])
        plugin.dismissLoading(makeDismissCall())
        await flushMainQueue()

        XCTAssertEqual(results, [true])
    }

    private func makeLoadingCall(_ callbackId: String, resolve: @escaping (Bool) -> Void) -> CAPPluginCall {
        return CAPPluginCall(callbackId: callbackId, methodName: "showLoading", options: [:], success: { result, _ in
            guard let cancelled = result?.data?["cancelled"] as? Bool else {
                return XCTFail("Expected a loading result")
            }
            resolve(cancelled)
        }, error: { error in
            XCTFail("Unexpected rejection: \(String(describing: error))")
        })
    }

    private func makeDismissCall() -> CAPPluginCall {
        return CAPPluginCall(callbackId: UUID().uuidString, methodName: "dismissLoading", options: [:], success: { _, _ in }, error: { error in
            XCTFail("Unexpected rejection: \(String(describing: error))")
        })
    }

    private func makeCall(_ callbackId: String, timeout: Double? = nil, resolve: @escaping (Bool) -> Void) -> CAPPluginCall {
        var options: [String: Any] = [:]
        if let timeout = timeout { options["timeout"] = timeout }
        return CAPPluginCall(callbackId: callbackId, methodName: "confirm", options: options, success: { result, _ in
            guard let value = result?.data?["value"] as? Bool else {
                return XCTFail("Expected a confirm result")
            }
            resolve(value)
        }, error: { error in
            XCTFail("Unexpected rejection: \(String(describing: error))")
        })
    }

    private func flushMainQueue() async {
        await withCheckedContinuation { continuation in
            DispatchQueue.main.async { continuation.resume() }
        }
    }
}

private class DeferredDismissalPlugin: ExtendedDialogPlugin {
    private var completions: [() -> Void] = []

    override func closePresentedDialog(then next: @escaping () -> Void) {
        completions.append(next)
    }

    var pendingDismissals: Int { completions.count }

    func finishDismissals() {
        while !completions.isEmpty { completions.removeFirst()() }
    }
}
