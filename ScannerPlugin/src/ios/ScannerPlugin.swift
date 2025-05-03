import AVFoundation
import UIKit

/// Scanner plugin that handles barcode and QR code scanning functionality
@objc(ScannerPlugin) class ScannerPlugin: CDVPlugin {
    // MARK: - Properties

    /// Callback ID for the current scanning operation
    private var commandCallbackId: String?

    /// Camera capture session
    internal var captureSession: AVCaptureSession?

    /// Preview layer for camera feed
    internal var previewLayer: AVCaptureVideoPreviewLayer?

    /// Scanner view controller
    internal var scannerViewController: UIViewController?

    /// UI Elements
    internal var scanningTargetView: UIView?
    internal var flashlightButton: UIButton?
    internal var bottomTextLabel: UILabel?
    internal var scanningLine: UIView?
    internal var scanningAnimation: CABasicAnimation?

    /// Detected codes and their visual indicators
    internal var detectedCodes: [AVMetadataMachineReadableCodeObject] = []
    internal var codeDots: [UIView] = []

    // MARK: - Constants

    internal enum Constants {
        static let targetSize: CGFloat = 280
        static let cornerLength: CGFloat = 20
        static let cornerWidth: CGFloat = 4
        static let buttonSize: CGFloat = 44
        static let dotSize: CGFloat = 38
        static let dotBorderWidth: CGFloat = 4
        static let scanningLineHeight: CGFloat = 3
        static let scanningAnimationDuration: CFTimeInterval = 2.5
        static let bounceAnimationDuration: CFTimeInterval = 0.8
        static let resultDelay: TimeInterval = 0.5

        static let blueColor = UIColor(red: 0/255, green: 117/255, blue: 184/255, alpha: 1.0)
        static let blueColorAlpha = UIColor(red: 0/255, green: 117/255, blue: 184/255, alpha: 0.8)
        static let overlayColor = UIColor(white: 0, alpha: 0.5)
        static let buttonBackgroundColor = UIColor(white: 0.2, alpha: 0.7)
        static let activeButtonBackgroundColor = UIColor(red: 0/255, green: 117/255, blue: 184/255, alpha: 0.7)
    }

    // MARK: - Public Methods

    /// Starts the scanning process
    /// - Parameter command: The command containing the bottom text to display
    @objc(scan:)
    func scan(command: CDVInvokedUrlCommand) {
        self.commandCallbackId = command.callbackId
        let bottomText = command.argument(at: 0) as? String ?? ""

        codeDots.forEach { $0.removeFromSuperview() }
        codeDots.removeAll()
        detectedCodes.removeAll()

        DispatchQueue.main.async {
            self.startScanning(bottomText: bottomText)
        }
    }

    // MARK: - Private Methods

    /// Initializes and starts the scanning process
    /// - Parameter bottomText: Text to display at the bottom of the scanner
    private func startScanning(bottomText: String) {
        guard let videoCaptureDevice = AVCaptureDevice.default(for: .video) else {
            sendError("Camera not available")
            return
        }

        let session = setupCaptureSession(with: videoCaptureDevice)
        let scannerVC = createScannerViewController(session: session, bottomText: bottomText)
        scannerVC.modalPresentationStyle = .fullScreen

        // Start the session before presenting the view controller
        session.startRunning()

        // Present the view controller with animation
        self.viewController.present(scannerVC, animated: true) {
            // Ensure the preview layer is properly sized
            self.previewLayer?.frame = scannerVC.view.layer.bounds
        }

        self.captureSession = session
        self.scannerViewController = scannerVC
    }

    /// Sets up the capture session with the given device
    /// - Parameter device: The video capture device to use
    /// - Returns: Configured capture session
    private func setupCaptureSession(with device: AVCaptureDevice) -> AVCaptureSession {
        let session = AVCaptureSession()

        guard let videoInput = try? AVCaptureDeviceInput(device: device),
              session.canAddInput(videoInput) else {
            sendError("Failed to create video input")
            return session
        }
        session.addInput(videoInput)

        let metadataOutput = AVCaptureMetadataOutput()
        guard session.canAddOutput(metadataOutput) else {
            sendError("Failed to add metadata output")
            return session
        }

        session.addOutput(metadataOutput)
        metadataOutput.setMetadataObjectsDelegate(self, queue: DispatchQueue.main)
        metadataOutput.metadataObjectTypes = [
            .qr,
            .code128,
            .code39,
            .code93,
            .ean8,
            .ean13,
            .interleaved2of5,
            .pdf417,
            .upce
        ]

        return session
    }

    /// Creates and configures the scanner view controller
    /// - Parameters:
    ///   - session: The capture session to use
    ///   - bottomText: Text to display at the bottom of the scanner
    /// - Returns: Configured view controller
    private func createScannerViewController(session: AVCaptureSession, bottomText: String) -> UIViewController {
        let scannerVC = UIViewController()

        // Setup preview layer
        self.previewLayer = AVCaptureVideoPreviewLayer(session: session)
        self.previewLayer?.frame = scannerVC.view.layer.bounds
        self.previewLayer?.videoGravity = .resizeAspectFill
        scannerVC.view.layer.addSublayer(self.previewLayer!)

        // Add UI elements
        addOverlay(to: scannerVC.view)
        addScanningTarget(to: scannerVC.view)
        addFlashlightButton(to: scannerVC.view)
        addBottomTextLabel(to: scannerVC.view, text: bottomText)
        addCloseButton(to: scannerVC.view)

        // Add gesture recognizers
        let tapGesture = UITapGestureRecognizer(target: self, action: #selector(handleTap(_:)))
        scannerVC.view.addGestureRecognizer(tapGesture)

        let pinchGesture = UIPinchGestureRecognizer(target: self, action: #selector(handlePinch(_:)))
        scannerVC.view.addGestureRecognizer(pinchGesture)

        // Set the scanning area after the preview layer is configured - used to restrict scanning aread (1/2)
        if let metadataOutput = session.outputs.first as? AVCaptureMetadataOutput {
            let targetFrame = scanningTargetView?.frame ?? CGRect.zero
            let rectOfInterest = self.previewLayer?
                .metadataOutputRectConverted(fromLayerRect: targetFrame)
                ?? CGRect.zero

            metadataOutput.rectOfInterest = rectOfInterest
        }

        return scannerVC
    }

    /// Adds the semi-transparent overlay to the view
    /// - Parameter view: The view to add the overlay to
    private func addOverlay(to view: UIView) {
        let overlayView = UIView(frame: view.bounds)
        overlayView.backgroundColor = Constants.overlayColor
        overlayView.tag = 100
        view.addSubview(overlayView)
    }

    /// Adds the flashlight button
    /// - Parameter view: The view to add the button to
    private func addFlashlightButton(to view: UIView) {
        let buttonSize: CGFloat = Constants.buttonSize
        let flashlightButton = UIButton(frame: CGRect(x: 0, y: 0, width: buttonSize, height: buttonSize))
        // Calculate Y position based on target view's position
        let targetViewY = view.bounds.height * 0.35
        let targetViewHeight = Constants.targetSize
        flashlightButton.center = CGPoint(x: view.bounds.midX, y: targetViewY + targetViewHeight/2 + 60)
        flashlightButton.backgroundColor = Constants.buttonBackgroundColor
        flashlightButton.layer.cornerRadius = buttonSize / 2

        let config = UIImage.SymbolConfiguration(pointSize: 24, weight: .medium)
        flashlightButton.setImage(UIImage(systemName: "flashlight.off.fill", withConfiguration: config), for: .normal)
        flashlightButton.tintColor = .white
        flashlightButton.addTarget(self, action: #selector(toggleFlashlight), for: .touchUpInside)

        view.addSubview(flashlightButton)
        self.flashlightButton = flashlightButton
    }

    /// Adds the bottom text label
    /// - Parameters:
    ///   - view: The view to add the label to
    ///   - text: The text to display
    private func addBottomTextLabel(to view: UIView, text: String) {
        let bottomLabel = PaddedLabel()
        bottomLabel.text = text
        bottomLabel.textColor = .white
        bottomLabel.textAlignment = .center
        bottomLabel.font = UIFont.systemFont(ofSize: 16, weight: .medium)
        bottomLabel.numberOfLines = 0
        bottomLabel.backgroundColor = .clear
        bottomLabel.layer.cornerRadius = 0
        bottomLabel.layer.masksToBounds = true
        bottomLabel.padding = .zero

        let maxWidth = view.bounds.width - 20
        let labelSize = bottomLabel.sizeThatFits(CGSize(width: maxWidth, height: .greatestFiniteMagnitude))
        bottomLabel.frame = CGRect(x: 10, y: view.bounds.height - labelSize.height - 40,
                                 width: maxWidth, height: labelSize.height)

        view.addSubview(bottomLabel)
        view.bringSubviewToFront(bottomLabel)
        self.bottomTextLabel = bottomLabel
    }

    /// Creates a dot view for detected codes
    /// - Parameter point: The center point for the dot
    /// - Returns: Configured dot view
    internal func createDotView(at point: CGPoint) -> UIView {
        let dotView = UIView(frame: CGRect(x: point.x - Constants.dotSize/2,
                                         y: point.y - Constants.dotSize/2,
                                         width: Constants.dotSize,
                                         height: Constants.dotSize))
        dotView.backgroundColor = Constants.blueColor
        dotView.layer.cornerRadius = Constants.dotSize/2
        dotView.layer.borderWidth = Constants.dotBorderWidth
        dotView.layer.borderColor = UIColor.white.cgColor

        let bounceAnimation = createBounceAnimation()
        dotView.layer.add(bounceAnimation, forKey: "bounce")

        return dotView
    }

    /// Creates the bounce animation for dots
    /// - Returns: Configured animation
    private func createBounceAnimation() -> CAKeyframeAnimation {
        let animation = CAKeyframeAnimation(keyPath: "transform.scale")
        animation.values = [1.0, 1.2, 1.0]
        animation.keyTimes = [0, 0.5, 1]
        animation.duration = Constants.bounceAnimationDuration
        animation.repeatCount = .infinity
        animation.timingFunction = CAMediaTimingFunction(name: .easeInEaseOut)
        return animation
    }

    /// Shows a focus animation at the specified point
    /// - Parameter point: The point to show the focus animation at
    internal func showFocusAnimation(at point: CGPoint) {
        let focusSize: CGFloat = 70
        let focusView = UIView(frame: CGRect(x: 0, y: 0, width: focusSize, height: focusSize))
        focusView.center = point
        focusView.backgroundColor = .clear
        focusView.layer.borderColor = Constants.blueColor.cgColor
        focusView.layer.borderWidth = 1
        focusView.layer.cornerRadius = focusSize / 2
        focusView.alpha = 0

        self.scannerViewController?.view.addSubview(focusView)

        // Animate focus view
        UIView.animate(withDuration: 0.3, animations: {
            focusView.alpha = 1
            focusView.transform = CGAffineTransform(scaleX: 0.8, y: 0.8)
        }, completion: { _ in
            UIView.animate(withDuration: 0.2, delay: 0.5, options: [], animations: {
                focusView.alpha = 0
                focusView.transform = CGAffineTransform(scaleX: 1.2, y: 1.2)
            }, completion: { _ in
                focusView.removeFromSuperview()
            })
        })
    }

    /// Toggles the flashlight on/off
    @objc private func toggleFlashlight() {
        guard let device = AVCaptureDevice.default(for: .video),
              device.hasTorch else { return }

        do {
            try device.lockForConfiguration()
            let config = UIImage.SymbolConfiguration(pointSize: 24, weight: .medium)

            if device.torchMode == .off {
                try device.setTorchModeOn(level: 1.0)
                let flashlightImage = UIImage(
                    systemName: "flashlight.on.fill",
                    withConfiguration: config
                )

                flashlightButton?.setImage(flashlightImage, for: .normal)
                flashlightButton?.backgroundColor = Constants.activeButtonBackgroundColor
            } else {
                device.torchMode = .off
                let flashlightImage = UIImage(
                    systemName: "flashlight.off.fill",
                    withConfiguration: config
                )

                flashlightButton?.setImage(flashlightImage, for: .normal)
                flashlightButton?.backgroundColor = Constants.buttonBackgroundColor
            }
            device.unlockForConfiguration()
        } catch {
            print("Error toggling flashlight: \(error)")
        }
    }

    /// Adds a close button to the view
    /// - Parameter view: The view to add the button to
    private func addCloseButton(to view: UIView) {
        // Reduce the size by 6, otherwise it looks bigger than flashlight button, although they have the same size
        let buttonSize: CGFloat = Constants.buttonSize - 6
        let closeButton = UIButton(frame: CGRect(x: 0, y: 0, width: buttonSize, height: buttonSize))
        closeButton.center = CGPoint(x: view.bounds.width - buttonSize/2 - 16, y: buttonSize/2 + 50)
        closeButton.backgroundColor = Constants.buttonBackgroundColor
        closeButton.layer.cornerRadius = buttonSize / 2

        let config = UIImage.SymbolConfiguration(pointSize: 20, weight: .medium)
        closeButton.setImage(UIImage(systemName: "xmark", withConfiguration: config), for: .normal)
        closeButton.tintColor = .white
        closeButton.addTarget(self, action: #selector(closeScanner), for: .touchUpInside)

        view.addSubview(closeButton)
    }

    /// Closes the scanner view controller
    @objc private func closeScanner() {
        self.captureSession?.stopRunning()
        self.scannerViewController?.dismiss(animated: true)
    }

    /// Sends a success result back to the plugin
    /// - Parameter result: The result string to send
    internal func sendSuccess(_ result: String) {
        let pluginResult = CDVPluginResult(status: .ok, messageAs: result)
        self.commandDelegate.send(pluginResult, callbackId: self.commandCallbackId)
    }

    /// Sends an error result back to the plugin
    /// - Parameter message: The error message to send
    private func sendError(_ message: String) {
        let pluginResult = CDVPluginResult(status: .error, messageAs: message)
        self.commandDelegate.send(pluginResult, callbackId: self.commandCallbackId)
    }
}
