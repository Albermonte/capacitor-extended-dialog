import Foundation

@objc public class ExtendedDialog: NSObject {
    @objc public func echo(_ value: String) -> String {
        print(value)
        return value
    }
}
