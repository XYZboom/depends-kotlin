package depends.extractor.kotlin.utils

import org.antlr.v4.runtime.RuleContext

inline fun <reified T : RuleContext> RuleContext?.getParentOfType(): T? {
    var now = this ?: return null
    while (now !is T) {
        now = now.parent
        if (now == null) return null
    }
    return now
}