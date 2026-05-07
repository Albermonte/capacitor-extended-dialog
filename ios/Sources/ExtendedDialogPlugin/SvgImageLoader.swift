import UIKit
import SVGKit

enum SvgImageLoader {

    private static let svgDataUrlPrefix = "data:image/svg+xml"
    private static let dataUrlPrefix = "data:"

    static func isDataUrl(_ source: String) -> Bool {
        source.range(of: dataUrlPrefix, options: [.anchored, .caseInsensitive]) != nil
    }

    static func isSvgSource(_ source: String) -> Bool {
        if source.range(of: svgDataUrlPrefix, options: [.anchored, .caseInsensitive]) != nil {
            return true
        }
        if let url = URL(string: source),
           let scheme = url.scheme?.lowercased(),
           scheme == "http" || scheme == "https" {
            return url.pathExtension.caseInsensitiveCompare("svg") == .orderedSame
        }
        return false
    }

    /// Supports `;base64` and percent-encoded payloads.
    static func decodeDataUrl(_ source: String) -> Data? {
        guard let commaIndex = source.firstIndex(of: ",") else { return nil }
        let header = source[..<commaIndex]
        let payload = String(source[source.index(after: commaIndex)...])
        if header.range(of: ";base64", options: .caseInsensitive) != nil {
            return Data(base64Encoded: payload)
        }
        return (payload.removingPercentEncoding ?? payload).data(using: .utf8)
    }

    /// Uses image's intrinsic size when `targetSize` is `.zero`.
    static func render(source: String, targetSize: CGSize, completion: @escaping (UIImage?) -> Void) {
        if isDataUrl(source) {
            guard let data = decodeDataUrl(source) else {
                completion(nil); return
            }
            DispatchQueue.global(qos: .userInitiated).async {
                let image = render(data: data, targetSize: targetSize)
                DispatchQueue.main.async { completion(image) }
            }
            return
        }
        guard let url = URL(string: source) else {
            completion(nil); return
        }
        URLSession.shared.dataTask(with: url) { data, _, _ in
            guard let data = data else {
                DispatchQueue.main.async { completion(nil) }
                return
            }
            DispatchQueue.global(qos: .userInitiated).async {
                let image = render(data: data, targetSize: targetSize)
                DispatchQueue.main.async { completion(image) }
            }
        }.resume()
    }

    private static func render(data: Data, targetSize: CGSize) -> UIImage? {
        guard let svgImage = SVGKImage(data: data) else { return nil }
        if targetSize.width > 0 && targetSize.height > 0 && svgImage.size != targetSize {
            svgImage.size = targetSize
        }
        return svgImage.uiImage
    }
}
