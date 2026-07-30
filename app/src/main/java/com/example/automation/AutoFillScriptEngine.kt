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

    // Direct click-to-fill into currently focused input or targeted field without coding
    fun buildInjectSingleValueScript(valueToFill: String): String {
        val escaped = valueToFill.replace("'", "\\'").replace("\n", " ")
        return """
            (async function() {
                try {
                    function sleep(minMs, maxMs) {
                        return new Promise(r => setTimeout(r, Math.floor(Math.random() * (maxMs - minMs + 1)) + minMs));
                    }

                    var activeEl = document.activeElement;
                    if (!activeEl || (activeEl.tagName !== 'INPUT' && activeEl.tagName !== 'TEXTAREA')) {
                        // Find first empty visible input field
                        var inputs = document.querySelectorAll('input:not([type="hidden"]), textarea');
                        for (var i = 0; i < inputs.length; i++) {
                            if (inputs[i].offsetParent !== null && !inputs[i].value) {
                                activeEl = inputs[i];
                                break;
                            }
                        }
                    }

                    if (!activeEl || (activeEl.tagName !== 'INPUT' && activeEl.tagName !== 'TEXTAREA')) {
                        return 'NO_INPUT_FOCUSED';
                    }

                    activeEl.scrollIntoView({ behavior: 'smooth', block: 'center' });
                    activeEl.focus();
                    await sleep(50, 100);

                    var nativeSetter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value') ?
                        Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set : null;

                    if (nativeSetter) { nativeSetter.call(activeEl, '$escaped'); } else { activeEl.value = '$escaped'; }
                    activeEl.dispatchEvent(new Event('input', { bubbles: true }));
                    activeEl.dispatchEvent(new Event('change', { bubbles: true }));
                    activeEl.style.border = '2px solid #10B981';
                    activeEl.style.backgroundColor = '#ECFDF5';

                    return 'CLICK_FILLED_SUCCESS';
                } catch(e) {
                    return 'ERROR: ' + e.message;
                }
            })();
        """.trimIndent()
    }

    // List of Token Registration Links (web.facebook.com, limited.facebook.com & m.facebook.com)
    val TOKEN_MREG_URLS = listOf(
        "https://limited.facebook.com/reg/?logger_id&is_two_steps_login=0&cid=103&next=https%3A%2F%2Fm.facebook.com%2Fconfirmemail.php%3Fnext%3Dhttps%253A%252F%252Fdevelopers.facebook.com%252Fdocumentation%252Ffacebook-login%252Fios%252Flimited-login%26http_ref%3DeyJ0cyI6IjE3ODQ4ODUxNjQ0NjMiLCJyIjoiaHR0cHM6XC9cL3d3dy5nb29nbGUuY29tXC8ifQ%253D%253D%26cah%3D2%26rwtsid%3DVMsmUcMfLX80RBelV&refsrc=deprecated&soft=hjk",
        "https://web.facebook.com/mreg?e_token=Abm-pjJYTVRotRwUm2mvUNcnlg29yW2EJDhquFUbW0XUm_CVx_mxwEam6UMxnehHvuFGPLASa2pmgA&d_hash=FBA71FDC8239E901",
        "https://web.facebook.com/mreg?e_token=AblG8NgRXtweT10VmrGryeTusY7yPTcp-YEfqQDK7R3YlKmypw9Ox4wsxK_-s82mFlfcrEwGwTOKNQ&d_hash=80AE5E5572F616E99079B0A2D3596C24",
        "https://web.facebook.com/mreg?e_token=AbnQFQG4x_sBJ1BS1HgYin1ijehpcfvN7TMPWiX9EUc3ccMDKbce7V9FPzk7AbMoPsA7K5nonavUvw&d_hash=FBA71FDC8239E901",
        "https://web.facebook.com/mreg?e_token=Abm6tgf10M_vK4TV2uawjG-ae8fFrddyzOf_FcUdJbRfjbkIcTrlIUJHoz7w6Vz4so8TOGWIcDgx0Q&d_hash=FBA71FDC8239E901",
        "https://web.facebook.com/mreg?e_token=Abky_3xr70NBQr4YJM2br-fChpjgWA3dJqiJ6Lm8JJP0XyXEiCg_RIWjY1OtPlcLRduFEgVzOSaDFA&d_hash=FBA71FDC8239E901",
        "https://m.facebook.com/mreg?e_token=Abm-pjJYTVRotRwUm2mvUNcnlg29yW2EJDhquFUbW0XUm_CVx_mxwEam6UMxnehHvuFGPLASa2pmgA&d_hash=FBA71FDC8239E901",
        "https://m.facebook.com/mreg?e_token=AblG8NgRXtweT10VmrGryeTusY7yPTcp-YEfqQDK7R3YlKmypw9Ox4wsxK_-s82mFlfcrEwGwTOKNQ&d_hash=80AE5E5572F616E99079B0A2D3596C24&cid=256002347743983&app_version=310&tg=201&cct=1&src=1&soft=hjk",
        "https://m.facebook.com/mreg?e_token=AbliUrkbMtSgUBgB0Lh6uh-W5ZR_QiE1rB6pQ8mWYPiNQNIoVH7cnQaPiPq6ufSFa5IxfSeOoqHErg&d_hash=FBA71FDC8239E901"
    )

    fun getRandomTokenUrl(): String {
        return TOKEN_MREG_URLS.random()
    }

    // Zero-click Smart Auto-Signup Script: Fills Name -> Auto Next -> DOB & Gender -> Auto Next -> Focus Phone -> Typing phone auto-clicks Next & Password & Sign Up!
    fun buildOneTapSmartAutoSignupScript(profile: GeneratedProfile): String {
        return """
            (async function() {
                try {
                    function sleep(minMs, maxMs) {
                        return new Promise(r => setTimeout(r, Math.floor(Math.random() * (maxMs - minMs + 1)) + minMs));
                    }

                    var nativeSetter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value') ?
                        Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set : null;

                    function setVal(el, val) {
                        if (!el) return;
                        if (nativeSetter) { nativeSetter.call(el, val); } else { el.value = val; }
                        el.dispatchEvent(new Event('input', { bubbles: true }));
                        el.dispatchEvent(new Event('change', { bubbles: true }));
                        el.style.border = '2px solid #10B981';
                        el.style.backgroundColor = '#ECFDF5';
                    }

                    function triggerEnterOnField(el) {
                        if (!el) return;
                        try {
                            el.focus();
                            var enterEvt = new KeyboardEvent('keydown', { key: 'Enter', code: 'Enter', keyCode: 13, which: 13, bubbles: true });
                            el.dispatchEvent(enterEvt);
                            var enterUpEvt = new KeyboardEvent('keyup', { key: 'Enter', code: 'Enter', keyCode: 13, which: 13, bubbles: true });
                            el.dispatchEvent(enterUpEvt);

                            var form = el.form || el.closest('form');
                            if (form) {
                                if (typeof form.requestSubmit === 'function') {
                                    form.requestSubmit();
                                } else if (typeof form.submit === 'function') {
                                    form.submit();
                                }
                            }
                        } catch(e) {}
                    }

                    function clickElement(el) {
                        if (!el) return false;
                        try {
                            el.scrollIntoView({ behavior: 'smooth', block: 'center' });
                            if (typeof el.focus === 'function') el.focus();
                            el.click();
                            
                            var evtOptions = { bubbles: true, cancelable: true, view: window };
                            el.dispatchEvent(new MouseEvent('mousedown', evtOptions));
                            el.dispatchEvent(new MouseEvent('mouseup', evtOptions));
                            el.dispatchEvent(new MouseEvent('click', evtOptions));
                            el.dispatchEvent(new PointerEvent('pointerdown', evtOptions));
                            el.dispatchEvent(new PointerEvent('pointerup', evtOptions));

                            var parentForm = el.form || (el.closest ? el.closest('form') : null);
                            if (parentForm) {
                                if (typeof parentForm.requestSubmit === 'function') {
                                    try { parentForm.requestSubmit(); } catch(err) {}
                                }
                            }
                            return true;
                        } catch(e) {
                            return false;
                        }
                    }

                    function findNextButton() {
                        // 1. Direct query by standard Facebook attributes
                        var directBtn = document.querySelector(
                            "button[name='websubmit'], button[type='submit'], input[type='submit'], " +
                            "button[id*='next' i], button[id*='submit' i], button[id*='signup' i], " +
                            "div[data-sigil*='next'], div[data-sigil*='signup'], div[data-sigil*='submit'], " +
                            "button[data-sigil*='next'], button[data-sigil*='signup']"
                        );
                        if (directBtn) return directBtn;

                        // 2. Query all clickable candidate elements
                        var candidates = document.querySelectorAll("button, input[type='submit'], input[type='button'], div[role='button'], a[role='button'], span[role='button'], div[class*='button' i], button[class*='btn' i]");
                        for (var i = 0; i < candidates.length; i++) {
                            var txt = (candidates[i].innerText || candidates[i].value || candidates[i].getAttribute('aria-label') || '').toLowerCase().trim();
                            if (
                                txt.includes('next') || txt.includes('continue') || 
                                txt.includes('পরবর্তী') || txt.includes('এগিয়ে') || txt.includes('এগিয়ে') ||
                                txt.includes('মেইল') || txt.includes('ফোন') || txt.includes('sign up') || txt.includes('submit')
                            ) {
                                return candidates[i];
                            }
                        }

                        // 3. Fallback to any primary button in form
                        return document.querySelector("form button, form input[type='submit']");
                    }

                    function findSubmitButton() {
                        var submitBtn = document.querySelector("button[name='websubmit'], button[type='submit'], input[type='submit'], button[id*='signup' i]");
                        if (submitBtn) return submitBtn;

                        var candidates = document.querySelectorAll("button, input[type='submit'], div[role='button'], a[role='button']");
                        for (var i = 0; i < candidates.length; i++) {
                            var txt = (candidates[i].innerText || candidates[i].value || candidates[i].getAttribute('aria-label') || '').toLowerCase().trim();
                            if (txt.includes('sign up') || txt.includes('submit') || txt.includes('register') || txt.includes('সাইন আপ') || txt.includes('তৈরি করুন')) {
                                return candidates[i];
                            }
                        }
                        return findNextButton();
                    }

                    var nameStepDone = false;
                    var dobStepDone = false;
                    var popupHandled = false;

                    async function processCurrentStep() {
                        // 0. Handle Popup (OK click) if visible at any point
                        var allBtns = document.querySelectorAll("button, div[role='button'], a[role='button'], input[type='button'], input[type='submit']");
                        for (var k = 0; k < allBtns.length; k++) {
                            var bTxt = (allBtns[k].innerText || allBtns[k].value || '').toLowerCase().trim();
                            if (bTxt === 'ok' || bTxt === 'ঠিক আছে' || bTxt === 'অকে') {
                                clickElement(allBtns[k]);
                                popupHandled = true;
                                await sleep(300, 600);
                                break;
                            }
                        }

                        // STEP 1: First Name & Last Name (Surname) -> Click Next
                        var fnEl = document.querySelector("input[name='firstname'], input[name*='first' i], input[placeholder*='First' i]");
                        var lnEl = document.querySelector("input[name='lastname'], input[name*='last' i], input[placeholder*='Surname' i], input[placeholder*='Last' i]");
                        var dayEl = document.querySelector("select[name='birthday_day'], #day");

                        if (fnEl && !nameStepDone) {
                            setVal(fnEl, '${profile.firstName}');
                            if (lnEl) setVal(lnEl, '${profile.lastName}');
                            nameStepDone = true;

                            // If DOB select is NOT on this screen, click Next
                            if (!dayEl) {
                                await sleep(400, 700);
                                var nxtBtn = findNextButton();
                                if (nxtBtn) { clickElement(nxtBtn); return; }
                            }
                        }

                        // STEP 2: Date of Birth -> Click Next 2 times -> Age (20-40) -> Click Next
                        var ageEl = document.querySelector("input[name='age'], input[id*='age' i], input[placeholder*='age' i], input[name*='birthday_age' i]");
                        if ((dayEl || ageEl) && !dobStepDone) {
                            if (dayEl && !ageEl) {
                                // Click Next 2 times sequentially to bring up age entry
                                var nxt1 = findNextButton();
                                if (nxt1) clickElement(nxt1);
                                await sleep(300, 500);
                                var nxt2 = findNextButton();
                                if (nxt2) clickElement(nxt2);
                                await sleep(500, 800);
                            }

                            // Re-check for age input box or set year fallback
                            var ageInput = document.querySelector("input[name='age'], input[id*='age' i], input[placeholder*='age' i], input[name*='birthday_age' i], input[type='number']");
                            var randomAge = Math.floor(Math.random() * 21) + 20; // Age 20 to 40

                            if (ageInput) {
                                setVal(ageInput, randomAge.toString());
                            } else if (dayEl) {
                                var yearEl = document.querySelector("select[name='birthday_year'], #year");
                                if (yearEl) {
                                    var currYear = new Date().getFullYear();
                                    yearEl.value = (currYear - randomAge).toString();
                                    yearEl.dispatchEvent(new Event('change', { bubbles: true }));
                                }
                            }

                            // Also select gender if available
                            var radios = document.querySelectorAll("input[type='radio'][name='sex'], input[type='radio'][name='gender']");
                            if (radios.length > 0) {
                                var isFemale = '${profile.gender.lowercase()}' === 'female';
                                var targetRadio = isFemale ? radios[0] : (radios.length > 1 ? radios[1] : radios[0]);
                                targetRadio.checked = true;
                                targetRadio.click();
                                targetRadio.dispatchEvent(new Event('change', { bubbles: true }));
                            }

                            dobStepDone = true;
                            await sleep(400, 700);
                            var nxt3 = findNextButton();
                            if (nxt3) { clickElement(nxt3); return; }
                        }

                        // STEP 3: Password from setting -> auto-fill password field
                        var pwEl = document.querySelector("input[name='reg_passwd__'], input[type='password'], input[name*='pass' i]");
                        if (pwEl && !pwEl.value) {
                            setVal(pwEl, '${profile.password}');
                        }

                        // STEP 4: Phone number field focus & auto-fill password + Sign Up when phone entered/Next clicked
                        var epEl = document.querySelector("input[name='reg_email__'], input[type='tel'], input[type='email'], input[name*='email' i], input[name*='phone' i], input[name*='contact' i]");
                        if (epEl) {
                            if (!epEl.value) {
                                epEl.scrollIntoView({ behavior: 'smooth', block: 'center' });
                                epEl.focus();
                                epEl.style.border = '3px solid #2563EB';
                            }

                            if (!epEl.__attachedSubmit) {
                                epEl.__attachedSubmit = true;

                                function doSignUpSubmit() {
                                    setTimeout(async function() {
                                        var passField = document.querySelector("input[name='reg_passwd__'], input[type='password'], input[name*='pass' i]");
                                        if (passField) setVal(passField, '${profile.password}');
                                        await sleep(300, 600);
                                        var submitBtn = findSubmitButton();
                                        if (submitBtn) clickElement(submitBtn);
                                    }, 200);
                                }

                                epEl.addEventListener('input', function(e) {
                                    var val = e.target.value.replace(/[^0-9]/g, '');
                                    if (val.length >= 10 || e.target.value.includes('@')) {
                                        doSignUpSubmit();
                                    }
                                });
                            }
                        }
                    }

                    // Run step processor
                    await processCurrentStep();

                    // Step monitoring loop for page transitions
                    if (!window.__autoStepInterval) {
                        var stepCount = 0;
                        window.__autoStepInterval = setInterval(async function() {
                            stepCount++;
                            if (stepCount > 40) {
                                clearInterval(window.__autoStepInterval);
                                window.__autoStepInterval = null;
                                return;
                            }
                            await processCurrentStep();
                        }, 500);
                    }

                    return 'ZERO_CLICK_STEP_AUTOFILL_SUCCESS';
                } catch(e) {
                    return 'ERROR: ' + e.message;
                }
            })();
        """.trimIndent()
    }

    fun buildAutoFillScript(
        password: String,
        profile: GeneratedProfile = ProfileGenerator.generate(
            customPassword = password,
            passwordMode = ProfileGenerator.PasswordMode.CUSTOM_FIXED
        )
    ): String {
        val safePassword = password.replace("\\", "\\\\").replace("'", "\\'").replace("\"", "\\\"").replace("\n", "").replace("\r", "")
        val safeFirstName = profile.firstName.replace("\\", "\\\\").replace("'", "\\'").replace("\"", "\\\"").replace("\n", "").replace("\r", "")
        val safeLastName = profile.lastName.replace("\\", "\\\\").replace("'", "\\'").replace("\"", "\\\"").replace("\n", "").replace("\r", "")

        return """
            (function() {
                if (window.__autoFillTimer) {
                    clearInterval(window.__autoFillTimer);
                }

                function setVal(selArray, val) {
                    var el = null;
                    if (Array.isArray(selArray)) {
                        for (var i = 0; i < selArray.length; i++) {
                            el = document.querySelector(selArray[i]);
                            if (el) break;
                        }
                    } else if (typeof selArray === 'string') {
                        el = document.querySelector(selArray);
                    } else {
                        el = selArray;
                    }

                    if (el) {
                        if (el.value !== val) {
                            el.value = val;
                            el.dispatchEvent(new Event('input', { bubbles: true }));
                            el.dispatchEvent(new Event('change', { bubbles: true }));
                            el.dispatchEvent(new Event('blur', { bubbles: true }));
                            el.style.border = '2px solid #10B981';
                        }
                        return true;
                    }
                    return false;
                }

                function clickBtn() {
                    var b = document.querySelector('button[name="submit"]') || 
                            document.querySelector('button.primary') || 
                            document.querySelector('button[type="submit"]') || 
                            document.querySelector('input[type="submit"]') || 
                            (function() {
                                var btns = document.querySelectorAll('button, input[type="button"], input[type="submit"], a[role="button"], div[role="button"]');
                                for (var i = 0; i < btns.length; i++) {
                                    var text = (btns[i].innerText || btns[i].value || '').toLowerCase();
                                    if (text.includes('next') || text.includes('sign up') || text.includes('continue') || text.includes('পরবর্তী') || text.includes('সাইন আপ') || text.includes('आगे')) {
                                        return btns[i];
                                    }
                                }
                                return null;
                            })();
                    if (b) {
                        b.click();
                        return true;
                    }
                    return false;
                }

                var stepHandled = { name: false, dob: false, gender: false, password: false, phoneListener: false };
                var attempts = 0;

                window.__autoFillTimer = setInterval(function() {
                    attempts++;
                    if (attempts > 60) {
                        clearInterval(window.__autoFillTimer);
                        return;
                    }

                    // 1. First Name & Last Name Step
                    var fnEl = document.querySelector('input[name="firstname"], input[name*="first" i], input[autocomplete="given-name"]');
                    if (fnEl && !stepHandled.name) {
                        setVal(['input[name="firstname"]', 'input[name*="first" i]', 'input[autocomplete="given-name"]'], '$safeFirstName');
                        setVal(['input[name="lastname"]', 'input[name*="last" i]', 'input[autocomplete="family-name"]'], '$safeLastName');
                        stepHandled.name = true;
                        setTimeout(clickBtn, 350);
                        return;
                    }

                    // 2. Mobile Phone Number / Email Step Detection
                    var phoneEl = document.querySelector('input[name="reg_email__"], input[type="tel"], input[name*="phone" i], input[name*="contact" i]');
                    if (phoneEl && !stepHandled.phoneListener) {
                        stepHandled.phoneListener = true;
                        phoneEl.addEventListener('input', function() {
                            if (this.value && this.value.length >= 10) {
                                setTimeout(function() {
                                    clickBtn();
                                }, 800);
                            }
                        });
                        phoneEl.addEventListener('change', function() {
                            if (this.value && this.value.length >= 8) {
                                setTimeout(function() {
                                    clickBtn();
                                }, 500);
                            }
                        });
                    }

                    // 3. Date of Birth Step (Day, Month, Year)
                    var dayEl = document.querySelector('select[name="birthday_day"], select[id="day"], #day, select[name*="day" i]');
                    if (dayEl && !stepHandled.dob) {
                        setVal(['select[name="birthday_day"]', 'select[id="day"]', '#day', 'select[name*="day" i]'], '${profile.birthDay}');
                        setVal(['select[name="birthday_month"]', 'select[id="month"]', '#month', 'select[name*="month" i]'], '${profile.birthMonth}');
                        setVal(['select[name="birthday_year"]', 'select[id="year"]', '#year', 'select[name*="year" i]'], '${profile.birthYear}');
                        stepHandled.dob = true;
                        setTimeout(clickBtn, 350);
                        return;
                    }

                    // 4. Gender Step
                    var radios = document.querySelectorAll("input[type='radio'][name='sex'], input[type='radio'][name='gender']");
                    if (radios.length > 0 && !stepHandled.gender) {
                        var isFemale = '${profile.gender.lowercase()}' === 'female';
                        var targetRadio = isFemale ? radios[0] : (radios.length > 1 ? radios[1] : radios[0]);
                        if (targetRadio) {
                            targetRadio.checked = true;
                            targetRadio.click();
                            targetRadio.dispatchEvent(new Event('change', { bubbles: true }));
                        }
                        stepHandled.gender = true;
                        setTimeout(clickBtn, 350);
                        return;
                    }

                    // 5. Password Step
                    var passEl = document.querySelector('input[name="reg_passwd__"], input[type="password"], input[name*="pass" i]');
                    if (passEl && !stepHandled.password) {
                        setVal(['input[name="reg_passwd__"]', 'input[type="password"]', 'input[name*="pass" i]'], '$safePassword');
                        stepHandled.password = true;
                        setTimeout(clickBtn, 350);
                        return;
                    }
                }, 400);
            })();
        """.trimIndent()
    }
}


