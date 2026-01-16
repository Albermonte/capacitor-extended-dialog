// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "CapacitorExtendedDialog",
    platforms: [.iOS(.v15)],
    products: [
        .library(
            name: "CapacitorExtendedDialog",
            targets: ["ExtendedDialogPlugin"])
    ],
    dependencies: [
        .package(url: "https://github.com/ionic-team/capacitor-swift-pm.git", from: "8.0.0")
    ],
    targets: [
        .target(
            name: "ExtendedDialogPlugin",
            dependencies: [
                .product(name: "Capacitor", package: "capacitor-swift-pm"),
                .product(name: "Cordova", package: "capacitor-swift-pm")
            ],
            path: "ios/Sources/ExtendedDialogPlugin"),
        .testTarget(
            name: "ExtendedDialogPluginTests",
            dependencies: ["ExtendedDialogPlugin"],
            path: "ios/Tests/ExtendedDialogPluginTests")
    ]
)