import UIKit
import AVFoundation

extension ScannerPlugin {
    /// Adds the scanning target view with corner markers
    /// - Parameter view: The view to add the target to
    internal func addScanningTarget(to view: UIView) {
        let targetView = UIView(frame: CGRect(x: 0, y: 0, width: Constants.targetSize, height: Constants.targetSize))
        targetView.center = CGPoint(x: view.bounds.midX, y: view.bounds.height * 0.35)
        targetView.backgroundColor = .clear

        // Create mask for scanning area
        createScanningAreaMask(for: targetView, in: view)

        // Add corner markers
        addCornerMarkers(to: targetView)

        // Add scanning line
        addScanningLine(to: targetView)

        view.addSubview(targetView)
        self.scanningTargetView = targetView

        // Update scanning area after target view is added - used to restrict scanning area (2/2)
        if let metadataOutput = captureSession?.outputs.first as? AVCaptureMetadataOutput {
            let rectOfInterest = self.previewLayer?
                .metadataOutputRectConverted(fromLayerRect: targetView.frame)
                ?? CGRect.zero

            metadataOutput.rectOfInterest = rectOfInterest
        }
    }

    /// Creates the mask for the scanning area
    /// - Parameters:
    ///   - targetView: The target view
    ///   - view: The parent view
    private func createScanningAreaMask(for targetView: UIView, in view: UIView) {
        if let overlayView = view.viewWithTag(100) {
            let maskLayer = CAShapeLayer()
            let path = UIBezierPath(rect: overlayView.bounds)
            let scanRect = UIBezierPath(roundedRect: targetView.frame, cornerRadius: 0)
            path.append(scanRect)
            path.usesEvenOddFillRule = true
            maskLayer.path = path.cgPath
            maskLayer.fillRule = .evenOdd
            overlayView.layer.mask = maskLayer
        }
    }

    /// Adds corner markers to the target view
    /// - Parameter targetView: The target view to add markers to
    private func addCornerMarkers(to targetView: UIView) {
        let corners = [
            (start: CGPoint(x: 0, y: Constants.cornerLength),
             end: CGPoint(x: 0, y: 0),
             final: CGPoint(x: Constants.cornerLength, y: 0)),
            (start: CGPoint(x: Constants.targetSize - Constants.cornerLength, y: 0),
             end: CGPoint(x: Constants.targetSize, y: 0),
             final: CGPoint(x: Constants.targetSize, y: Constants.cornerLength)),
            (start: CGPoint(x: 0, y: Constants.targetSize - Constants.cornerLength),
             end: CGPoint(x: 0, y: Constants.targetSize),
             final: CGPoint(x: Constants.cornerLength, y: Constants.targetSize)),
            (start: CGPoint(x: Constants.targetSize - Constants.cornerLength, y: Constants.targetSize),
             end: CGPoint(x: Constants.targetSize, y: Constants.targetSize),
             final: CGPoint(x: Constants.targetSize, y: Constants.targetSize - Constants.cornerLength))
        ]

        for corner in corners {
            let shapeLayer = CAShapeLayer()
            let path = UIBezierPath()
            path.move(to: corner.start)
            path.addLine(to: corner.end)
            path.addLine(to: corner.final)
            shapeLayer.path = path.cgPath
            shapeLayer.strokeColor = Constants.blueColor.cgColor
            shapeLayer.lineWidth = Constants.cornerWidth
            shapeLayer.fillColor = UIColor.clear.cgColor
            targetView.layer.addSublayer(shapeLayer)
        }
    }

    /// Adds the scanning line with animation
    /// - Parameter targetView: The target view to add the line to
    private func addScanningLine(to targetView: UIView) {
        let scanningLine = UIView(
            frame: CGRect(
                x: 0,
                y: 0,
                width: Constants.targetSize,
                height: Constants.scanningLineHeight
            )
        )

        scanningLine.backgroundColor = Constants.blueColorAlpha
        scanningLine.layer.shadowColor = UIColor.white.cgColor
        scanningLine.layer.shadowOffset = .zero
        scanningLine.layer.shadowRadius = 2
        scanningLine.layer.shadowOpacity = 0.8
        targetView.addSubview(scanningLine)
        self.scanningLine = scanningLine

        let animation = createScanningAnimation()
        scanningLine.layer.add(animation, forKey: "scanning")
        self.scanningAnimation = animation
    }

    /// Creates the scanning line animation
    /// - Returns: Configured animation
    private func createScanningAnimation() -> CABasicAnimation {
        let animation = CABasicAnimation(keyPath: "transform.translation.y")
        animation.fromValue = 0
        animation.toValue = Constants.targetSize
        animation.duration = Constants.scanningAnimationDuration
        animation.repeatCount = .infinity
        animation.autoreverses = false
        animation.timingFunction = CAMediaTimingFunction(name: .easeInEaseOut)
        animation.isRemovedOnCompletion = false
        animation.fillMode = .forwards
        animation.beginTime = 0.1
        return animation
    }
}
