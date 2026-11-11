import XCTest
import SVGKit
@testable import ExtendedDialogPlugin

class SvgImageLoaderTests: XCTestCase {
    private struct Pixel {
        let red: UInt8
        let blue: UInt8
        let alpha: UInt8
    }

    // SVGKit's first parse in a Debug process fails: SVGLength's PPI lookup asserts on
    // device models it doesn't know (e.g. arm64 simulators). Absorb that before testing.
    override class func setUp() {
        super.setUp()
        _ = SVGKImage(data: Data("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"1\" height=\"1\">\n</svg>\n".utf8))
    }

    // Red rect fills the whole viewBox; blue dot sits at its center.
    private let negativeOriginSvg = """
    <svg xmlns="http://www.w3.org/2000/svg" width="512" height="512" viewBox="-4 -4 108 108">
    <rect x="-4" y="-4" width="108" height="108" fill="#FF0000"/>
    <circle cx="50" cy="50" r="6" fill="#0000FF"/>
    </svg>

    """

    private func render(_ source: String, size: CGSize) -> UIImage? {
        let done = expectation(description: "render")
        var result: UIImage?
        SvgImageLoader.render(source: source, targetSize: size) { image in
            result = image
            done.fulfill()
        }
        wait(for: [done], timeout: 5)
        return result
    }

    /// Pixel at a point, origin top-left.
    private func pixel(_ image: UIImage, at point: CGPoint) throws -> Pixel {
        let cgImage = try XCTUnwrap(image.cgImage)
        let width = cgImage.width
        let height = cgImage.height
        var data = [UInt8](repeating: 0, count: width * height * 4)
        let context = try XCTUnwrap(CGContext(data: &data, width: width, height: height, bitsPerComponent: 8,
                                              bytesPerRow: width * 4, space: CGColorSpaceCreateDeviceRGB(),
                                              bitmapInfo: CGImageAlphaInfo.premultipliedLast.rawValue))
        context.draw(cgImage, in: CGRect(x: 0, y: 0, width: width, height: height))
        let index = (Int(point.y * image.scale) * width + Int(point.x * image.scale)) * 4
        return Pixel(red: data[index], blue: data[index + 2], alpha: data[index + 3])
    }

    func testNegativeViewBoxOriginFitsTargetSize() throws {
        let size = CGSize(width: 64, height: 64)
        let source = "data:image/svg+xml;base64," + Data(negativeOriginSvg.utf8).base64EncodedString()
        let image = try XCTUnwrap(render(source, size: size))

        XCTAssertEqual(image.size, size)
        let corners = [CGPoint(x: 2, y: 2), CGPoint(x: 62, y: 2), CGPoint(x: 2, y: 62), CGPoint(x: 62, y: 62)]
        for corner in corners {
            let color = try pixel(image, at: corner)
            XCTAssertGreaterThan(color.alpha, 200, "corner \(corner) is transparent: content shifted or cropped")
            XCTAssertGreaterThan(color.red, 200, "corner \(corner) is not the background rect")
        }
        let center = try pixel(image, at: CGPoint(x: 32, y: 32))
        XCTAssertGreaterThan(center.blue, 200, "viewBox center is not at image center")
        XCTAssertLessThan(center.red, 60, "viewBox center is not at image center")
    }
}
