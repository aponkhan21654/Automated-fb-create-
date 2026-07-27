package com.example.automation

data class FieldSelectors(
    val firstNameSelectors: List<String> = listOf("input[name='firstname']", "#firstname", "input[placeholder*='First' i]", "input[name='firstname']"),
    val lastNameSelectors: List<String> = listOf("input[name='lastname']", "#lastname", "input[placeholder*='Surname' i]", "input[name='lastname']"),
    val emailPhoneSelectors: List<String> = listOf("input[name='reg_email__']", "input[type='tel']", "input[type='email']", "input[name='email']"),
    val passwordSelectors: List<String> = listOf("input[name='reg_passwd__']", "input[type='password']", "input[name='pass']"),
    val daySelectors: List<String> = listOf("select[name='birthday_day']", "#day", "select[aria-label*='Day' i]"),
    val monthSelectors: List<String> = listOf("select[name='birthday_month']", "#month", "select[aria-label*='Month' i]"),
    val yearSelectors: List<String> = listOf("select[name='birthday_year']", "#year", "select[aria-label*='Year' i]")
)

object AutoFillScriptEngine {

    // Advanced Human-like Auto-Fill Script Engine (Playwright/Selenium human behavior style)
    fun buildInjectScript(
        profile: GeneratedProfile,
        selectors: FieldSelectors = FieldSelectors(),
        humanLikeSpeedMs: Long = 40,
        autoSubmit: Boolean = false
    ): String {
        val fnSel = selectors.firstNameSelectors.joinToString("\", \"")
        val lnSel = selectors.lastNameSelectors.joinToString("\", \"")
        val epSel = selectors.emailPhoneSelectors.joinToString("\", \"")
        val pwSel = selectors.passwordSelectors.joinToString("\", \"")
        val daySel = selectors.daySelectors.joinToString("\", \"")
        val monthSel = selectors.monthSelectors.joinToString("\", \"")
        val yearSel = selectors.yearSelectors.joinToString("\", \"")

        return """
            (async function() {
                try {
                    // Helper to generate human-like random delay
                    function sleep(minMs, maxMs) {
                        var ms = Math.floor(Math.random() * (maxMs - minMs + 1)) + minMs;
                        return new Promise(resolve => setTimeout(resolve, ms));
                    }

                    // Stealth Anti-Bot Protection Overrides
                    try {
                        Object.defineProperty(navigator, 'webdriver', { get: () => undefined });
                        Object.defineProperty(navigator, 'languages', { get: () => ['en-US', 'en', 'bn'] });
                        Object.defineProperty(navigator, 'plugins', { get: () => [1, 2, 3, 4, 5] });
                    } catch(err) {}

                    function findFirst(selArray) {
                        for (var i = 0; i < selArray.length; i++) {
                            var el = document.querySelector(selArray[i]);
                            if (el && el.offsetParent !== null) return el;
                        }
                        for (var i = 0; i < selArray.length; i++) {
                            var el = document.querySelector(selArray[i]);
                            if (el) return el;
                        }
                        return null;
                    }

                    // Human-like character-by-character typing with event triggers
                    async function typeHumanLike(el, text) {
                        if (!el) return false;
                        
                        // 1. Smooth scroll to target field like a real user
                        el.scrollIntoView({ behavior: 'smooth', block: 'center' });
                        await sleep(150, 300);

                        // 2. Simulate human tap / focus
                        el.dispatchEvent(new MouseEvent('mousedown', { bubbles: true, cancelable: true }));
                        el.dispatchEvent(new MouseEvent('mouseup', { bubbles: true, cancelable: true }));
                        el.dispatchEvent(new Event('focus', { bubbles: true }));
                        el.focus();
                        await sleep(100, 250);

                        // Clear existing text
                        var nativeSetter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value') ?
                            Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set : null;

                        if (nativeSetter) { nativeSetter.call(el, ''); } else { el.value = ''; }
                        el.dispatchEvent(new Event('input', { bubbles: true }));

                        // 3. Type each character with variable human delay
                        var currentVal = '';
                        for (var i = 0; i < text.length; i++) {
                            var char = text.charAt(i);
                            currentVal += char;

                            if (nativeSetter) {
                                nativeSetter.call(el, currentVal);
                            } else {
                                el.value = currentVal;
                            }

                            // Trigger complete sequence of DOM key & input events
                            el.dispatchEvent(new KeyboardEvent('keydown', { key: char, bubbles: true }));
                            el.dispatchEvent(new KeyboardEvent('keypress', { key: char, bubbles: true }));
                            el.dispatchEvent(new Event('input', { bubbles: true }));
                            el.dispatchEvent(new KeyboardEvent('keyup', { key: char, bubbles: true }));

                            // Random typing delay per character (mimicking Playwright/Selenium human flow)
                            await sleep(35, 95);
                        }

                        el.dispatchEvent(new Event('change', { bubbles: true }));
                        el.dispatchEvent(new Event('blur', { bubbles: true }));
                        el.style.border = '2px solid #10B981';
                        el.style.backgroundColor = '#ECFDF5';
                        
                        // Micro pause after finishing field
                        await sleep(200, 450);
                        return true;
                    }

                    // Human-like select box option chooser
                    async function selectHumanLike(el, value) {
                        if (!el) return false;
                        el.scrollIntoView({ behavior: 'smooth', block: 'center' });
                        await sleep(100, 200);
                        el.focus();
                        el.value = value;
                        el.dispatchEvent(new Event('change', { bubbles: true }));
                        el.dispatchEvent(new Event('blur', { bubbles: true }));
                        el.style.border = '2px solid #10B981';
                        await sleep(150, 300);
                        return true;
                    }

                    // --- EXECUTE HUMAN TYPING SEQUENCE ---
                    var fnEl = findFirst(["$fnSel"]);
                    if (fnEl) await typeHumanLike(fnEl, "${profile.firstName}");

                    var lnEl = findFirst(["$lnSel"]);
                    if (lnEl) await typeHumanLike(lnEl, "${profile.lastName}");

                    var epEl = findFirst(["$epSel"]);
                    if (epEl) await typeHumanLike(epEl, "${profile.emailOrPhone}");

                    var pwEl = findFirst(["$pwSel"]);
                    if (pwEl) await typeHumanLike(pwEl, "${profile.password}");

                    var dayEl = findFirst(["$daySel"]);
                    if (dayEl) await selectHumanLike(dayEl, "${profile.birthDay}");

                    var monthEl = findFirst(["$monthSel"]);
                    if (monthEl) await selectHumanLike(monthEl, "${profile.birthMonth}");

                    var yearEl = findFirst(["$yearSel"]);
                    if (yearEl) await selectHumanLike(yearEl, "${profile.birthYear}");

                    return 'HUMAN_AUTOFILL_SUCCESS: Successfully typed form with realistic delays!';
                } catch(e) {
                    return 'ERROR: ' + e.message;
                }
            })();
        """.trimIndent()
    }
}

