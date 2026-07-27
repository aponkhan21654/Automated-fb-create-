package com.example.automation

data class FieldSelectors(
    val firstNameSelectors: List<String> = listOf("input[name='firstname']", "input[name*='first' i]", "input[id*='first' i]", "input[autocomplete='given-name']", "input[placeholder*='First' i]", "input[aria-label*='First' i]"),
    val lastNameSelectors: List<String> = listOf("input[name='lastname']", "input[name*='last' i]", "input[id*='last' i]", "input[id*='surname' i]", "input[autocomplete='family-name']", "input[placeholder*='Surname' i]", "input[placeholder*='Last' i]"),
    val emailPhoneSelectors: List<String> = listOf("input[name='reg_email__']", "input[name*='email' i]", "input[id*='email' i]", "input[type='email']", "input[type='tel']", "input[name*='phone' i]", "input[id*='phone' i]", "input[name*='contact' i]"),
    val passwordSelectors: List<String> = listOf("input[name='reg_passwd__']", "input[type='password']", "input[name*='pass' i]", "input[id*='pass' i]"),
    val daySelectors: List<String> = listOf("select[name='birthday_day']", "#day", "select[name*='day' i]", "select[id*='day' i]", "input[name*='day' i]", "select[aria-label*='Day' i]"),
    val monthSelectors: List<String> = listOf("select[name='birthday_month']", "#month", "select[name*='month' i]", "select[id*='month' i]", "input[name*='month' i]", "select[aria-label*='Month' i]"),
    val yearSelectors: List<String> = listOf("select[name='birthday_year']", "#year", "select[name*='year' i]", "select[id*='year' i]", "input[name*='year' i]", "select[aria-label*='Year' i]"),
    val genderSelectors: List<String> = listOf("input[name='sex']", "input[name='gender']", "select[name='sex']", "select[name='gender']"),
    val submitButtonSelectors: List<String> = listOf("button[name='websubmit']", "button[type='submit']", "input[type='submit']", "button[id*='signup' i]", "button[id*='submit' i]", "button[class*='signup' i]")
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
        val genderSel = selectors.genderSelectors.joinToString("\", \"")
        val submitSel = selectors.submitButtonSelectors.joinToString("\", \"")

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
                            if (!selArray[i] || selArray[i].trim() === '') continue;
                            var el = document.querySelector(selArray[i]);
                            if (el && el.offsetParent !== null) return el;
                        }
                        for (var i = 0; i < selArray.length; i++) {
                            if (!selArray[i] || selArray[i].trim() === '') continue;
                            var el = document.querySelector(selArray[i]);
                            if (el) return el;
                        }
                        return null;
                    }

                    // Human-like character-by-character typing with DOM Event Emitting
                    async function typeHumanLike(el, text) {
                        if (!el) return false;
                        
                        // 1. Smooth scroll to target field like a real user
                        el.scrollIntoView({ behavior: 'smooth', block: 'center' });
                        await sleep(120, 280);

                        // 2. Simulate human tap / focus
                        el.dispatchEvent(new MouseEvent('mousedown', { bubbles: true, cancelable: true }));
                        el.dispatchEvent(new MouseEvent('mouseup', { bubbles: true, cancelable: true }));
                        el.dispatchEvent(new Event('focus', { bubbles: true }));
                        el.focus();
                        await sleep(80, 200);

                        // Clear existing text via Native Setter
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

                            // Trigger complete sequence of DOM key & input events for React/Angular/Vue
                            el.dispatchEvent(new KeyboardEvent('keydown', { key: char, bubbles: true }));
                            el.dispatchEvent(new KeyboardEvent('keypress', { key: char, bubbles: true }));
                            el.dispatchEvent(new Event('input', { bubbles: true }));
                            el.dispatchEvent(new KeyboardEvent('keyup', { key: char, bubbles: true }));

                            // Random typing delay per character (mimicking Playwright/Selenium human flow)
                            await sleep(30, 85);
                        }

                        el.dispatchEvent(new Event('change', { bubbles: true }));
                        el.dispatchEvent(new Event('blur', { bubbles: true }));
                        el.style.border = '2px solid #10B981';
                        el.style.backgroundColor = '#ECFDF5';
                        
                        // Micro pause after finishing field
                        await sleep(180, 380);
                        return true;
                    }

                    // Human-like select box option chooser
                    async function selectHumanLike(el, value) {
                        if (!el) return false;
                        el.scrollIntoView({ behavior: 'smooth', block: 'center' });
                        await sleep(100, 200);
                        el.focus();
                        
                        // Try matching by option value or option text
                        var strVal = String(value);
                        var found = false;
                        for (var i = 0; i < el.options.length; i++) {
                            if (el.options[i].value === strVal || el.options[i].text.trim() === strVal) {
                                el.selectedIndex = i;
                                found = true;
                                break;
                            }
                        }
                        if (!found) { el.value = strVal; }

                        el.dispatchEvent(new Event('change', { bubbles: true }));
                        el.dispatchEvent(new Event('blur', { bubbles: true }));
                        el.style.border = '2px solid #10B981';
                        await sleep(120, 250);
                        return true;
                    }

                    // Human-like Gender radio or select handle
                    async function handleGenderHumanLike(genderStr) {
                        var isFemale = genderStr.toLowerCase() === 'female';
                        var isMale = genderStr.toLowerCase() === 'male';

                        // 1. Try finding radio buttons (Facebook sex: 1=Female, 2=Male)
                        var radios = document.querySelectorAll("input[type='radio'][name='sex'], input[type='radio'][name='gender']");
                        if (radios.length > 0) {
                            for (var r = 0; r < radios.length; r++) {
                                var val = radios[r].value;
                                var label = (radios[r].getAttribute('aria-label') || '').toLowerCase();
                                if ((isFemale && (val === '1' || label.includes('female'))) ||
                                    (isMale && (val === '2' || label.includes('male')))) {
                                    radios[r].scrollIntoView({ behavior: 'smooth', block: 'center' });
                                    await sleep(100, 200);
                                    radios[r].checked = true;
                                    radios[r].click();
                                    radios[r].dispatchEvent(new Event('change', { bubbles: true }));
                                    return true;
                                }
                            }
                        }

                        // 2. Try select dropdown
                        var genderSelect = findFirst(["$genderSel"]);
                        if (genderSelect && genderSelect.tagName === 'SELECT') {
                            await selectHumanLike(genderSelect, isFemale ? '1' : '2');
                            return true;
                        }

                        return false;
                    }

                    // --- EXECUTE HUMAN TYPING SEQUENCE ---
                    var fieldsFilled = 0;

                    var fnEl = findFirst(["$fnSel"]);
                    if (fnEl) { if (await typeHumanLike(fnEl, "${profile.firstName}")) fieldsFilled++; }

                    var lnEl = findFirst(["$lnSel"]);
                    if (lnEl) { if (await typeHumanLike(lnEl, "${profile.lastName}")) fieldsFilled++; }

                    var epEl = findFirst(["$epSel"]);
                    if (epEl) { if (await typeHumanLike(epEl, "${profile.emailOrPhone}")) fieldsFilled++; }

                    var pwEl = findFirst(["$pwSel"]);
                    if (pwEl) { if (await typeHumanLike(pwEl, "${profile.password}")) fieldsFilled++; }

                    var dayEl = findFirst(["$daySel"]);
                    if (dayEl) { if (await selectHumanLike(dayEl, "${profile.birthDay}")) fieldsFilled++; }

                    var monthEl = findFirst(["$monthSel"]);
                    if (monthEl) { if (await selectHumanLike(monthEl, "${profile.birthMonth}")) fieldsFilled++; }

                    var yearEl = findFirst(["$yearSel"]);
                    if (yearEl) { if (await selectHumanLike(yearEl, "${profile.birthYear}")) fieldsFilled++; }

                    await handleGenderHumanLike("${profile.gender}");

                    // Multi-step & Auto-Submit Handling
                    var autoSubmitted = false;
                    if (${autoSubmit}) {
                        await sleep(600, 1200);
                        var submitBtn = findFirst(["$submitSel"]);
                        if (submitBtn) {
                            submitBtn.scrollIntoView({ behavior: 'smooth', block: 'center' });
                            await sleep(200, 400);
                            submitBtn.click();
                            autoSubmitted = true;
                        }
                    }

                    return 'HUMAN_AUTOFILL_SUCCESS: Filled ' + fieldsFilled + ' fields' + (autoSubmitted ? ' & clicked Submit!' : '!');
                } catch(e) {
                    return 'ERROR: ' + e.message;
                }
            })();
        """.trimIndent()
    }
}


