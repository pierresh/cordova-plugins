import AVFoundation
import UIKit

// MARK: - AVCaptureMetadataOutputObjectsDelegate
extension ScannerPlugin: AVCaptureMetadataOutputObjectsDelegate {
    func metadataOutput(
        _ output: AVCaptureMetadataOutput,
        didOutput metadataObjects: [AVMetadataObject],
        from connection: AVCaptureConnection
    ) {
        // Clear previous dots
        codeDots.forEach { $0.removeFromSuperview() }
        codeDots.removeAll()
        detectedCodes.removeAll()

        // Process new QR codes
        for metadataObject in metadataObjects {
            if let codeObject = metadataObject as? AVMetadataMachineReadableCodeObject,
                codeObject.type == .qr ||
                codeObject.type == .code128 ||
                codeObject.type == .code39 ||
                codeObject.type == .code93 ||
                codeObject.type == .ean8 ||
                codeObject.type == .ean13 ||
                codeObject.type == .interleaved2of5 ||
                codeObject.type == .pdf417 ||
                codeObject.type == .upce {
                detectedCodes.append(codeObject)
            }
        }

        // Handle single code detection
        if detectedCodes.count == 1, let qrString = detectedCodes[0].stringValue {
            handleSingleCodeDetection(qrString)
            return
        }

        // Handle multiple codes detection
        if detectedCodes.count > 1 {
            handleMultipleCodesDetection()
        }
    }

    /// Handles the detection of a single code
    /// - Parameter qrString: The detected QR code string
    private func handleSingleCodeDetection(_ qrString: String) {
        // Stop the capture session to freeze the frame
        self.captureSession?.stopRunning()

        // Hide UI elements
        hideUIElements()

        // Show dot for the detected code
        let codeObject = detectedCodes[0]
        if let transformedObject = self.previewLayer?.transformedMetadataObject(for: codeObject) {
            let centerX = transformedObject.bounds.midX
            let centerY = transformedObject.bounds.midY
            let dotView = createDotView(at: CGPoint(x: centerX, y: centerY))
            self.scannerViewController?.view.addSubview(dotView)
            self.codeDots.append(dotView)
        }

        // Delay the result
        DispatchQueue.main.asyncAfter(deadline: .now() + Constants.resultDelay) {
            self.sendSuccess(qrString)
            self.scannerViewController?.dismiss(animated: true, completion: nil)
        }
    }

    /// Handles the detection of multiple codes
    private func handleMultipleCodesDetection() {
        // Stop the capture session to freeze the frame
        self.captureSession?.stopRunning()

        // Hide UI elements
        hideUIElements()

        // Show dots for each detected code
        for codeObject in detectedCodes {
            if let transformedObject = self.previewLayer?.transformedMetadataObject(for: codeObject) {
                let centerX = transformedObject.bounds.midX
                let centerY = transformedObject.bounds.midY
                let dotView = createDotView(at: CGPoint(x: centerX, y: centerY))
                self.scannerViewController?.view.addSubview(dotView)
                self.codeDots.append(dotView)
            }
        }
    }

    /// Hides all UI elements
    private func hideUIElements() {
        self.scanningTargetView?.isHidden = true
        self.flashlightButton?.isHidden = true
        self.bottomTextLabel?.isHidden = true
        self.scanningLine?.layer.removeAnimation(forKey: "scanning")

        if let overlayView = self.scannerViewController?.view.viewWithTag(100) {
            overlayView.removeFromSuperview()
        }
    }
}
