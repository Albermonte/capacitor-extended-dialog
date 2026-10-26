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

    func finishDismissals() {
        while !completions.isEmpty { completions.removeFirst()() }
    }
}
