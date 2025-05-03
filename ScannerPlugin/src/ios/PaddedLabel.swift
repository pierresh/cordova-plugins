import UIKit

/// A custom label that supports padding around its text
class PaddedLabel: UILabel {
    var padding: UIEdgeInsets = .zero

    override func textRect(forBounds bounds: CGRect, limitedToNumberOfLines numberOfLines: Int) -> CGRect {
        let insetRect = bounds.inset(by: padding)
        let textRect = super.textRect(forBounds: insetRect, limitedToNumberOfLines: numberOfLines)
        let invertedInsets = UIEdgeInsets(
            top: -padding.top,
            left: -padding.left,
            bottom: -padding.bottom,
            right: -padding.right
        )

        return textRect.inset(by: invertedInsets)
    }

    override func drawText(in rect: CGRect) {
        super.drawText(in: rect.inset(by: padding))
    }
}
