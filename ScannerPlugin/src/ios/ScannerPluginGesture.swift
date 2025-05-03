import UIKit
import AVFoundation

extension ScannerPlugin {
    /// Handles the tap gesture for focusing and code selection
    /// - Parameter gesture: The tap gesture recognizer
    @objc func handleTap(_ gesture: UITapGestureRecognizer) {
        let touchPoint = gesture.location(in: self.scannerViewController?.view)

        // First check if we tapped on a code dot
        var closestDot: (index: Int, distance: CGFloat)?
        for (index, dot) in codeDots.enumerated() {
            let dotCenter = CGPoint(x: dot.frame.midX, y: dot.frame.midY)
            let distance = hypot(touchPoint.x - dotCenter.x, touchPoint.y - dotCenter.y)
            if closestDot == nil || distance < closestDot!.distance {
                closestDot = (index, distance)
            }
        }

        // If we tapped near a code dot, handle code selection
        if let closest = closestDot, closest.distance < 30 {
            let selectedCode = detectedCodes[closest.index]
            if let qrString = selectedCode.stringValue {
                self.captureSession?.stopRunning()
                self.sendSuccess(qrString)
                self.scannerViewController?.dismiss(animated: true, completion: nil)
            }
            return
        }

        // If codes are detected, don't allow focusing
        if !detectedCodes.isEmpty { return }

        // Handle focus
        guard let device = AVCaptureDevice.default(for: .video) else { return }

        // Convert touch point to camera coordinates
        let focusPoint = self.previewLayer?
            .captureDevicePointConverted(fromLayerPoint: touchPoint)
            ?? CGPoint(x: 0.5, y: 0.5)

        do {
            try device.lockForConfiguration()

            // Check if the device supports focus
            if device.isFocusPointOfInterestSupported && device.isFocusModeSupported(.continuousAutoFocus) {
                device.focusPointOfInterest = focusPoint
                device.focusMode = .continuousAutoFocus
            }

            // Check if the device supports exposure
            if device.isExposurePointOfInterestSupported && device.isExposureModeSupported(.continuousAutoExposure) {
                device.exposurePointOfInterest = focusPoint
                device.exposureMode = .continuousAutoExposure
            }

            device.unlockForConfiguration()

            // Show focus animation
            showFocusAnimation(at: touchPoint)

        } catch {
            print("Error setting focus: \(error)")
        }
    }

    /// Handles the pinch gesture for zooming
    /// - Parameter gesture: The pinch gesture recognizer
    @objc func handlePinch(_ gesture: UIPinchGestureRecognizer) {
        guard let device = AVCaptureDevice.default(for: .video) else { return }

        func setZoom(_ zoom: CGFloat) {
            do {
                try device.lockForConfiguration()
                device.videoZoomFactor = max(1.0, min(zoom, device.activeFormat.videoMaxZoomFactor))
                device.unlockForConfiguration()
            } catch {
                print("Error setting zoom: \(error)")
            }
        }

        switch gesture.state {
        case .began:
            gesture.scale = device.videoZoomFactor
        case .changed:
            setZoom(gesture.scale)
        default:
            break
        }
    }
}
