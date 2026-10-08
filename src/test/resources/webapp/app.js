/*
 * Demo Patient Portal: a tiny, self-contained synthetic web app used as the system under test.
 * All users, providers and appointments are fictional. State lives in sessionStorage, so every
 * new browser session (one per scenario) starts clean.
 */
(function () {
    "use strict";

    // Synthetic demo accounts. Not real credentials; documented in the README.
    var USERS = {
        "patient.demo": { secret: "Demo!2026", displayName: "Alex Demo", locked: false }, // pragma: allowlist secret (synthetic demo account)
        "locked.demo": { secret: "Demo!2026", displayName: "Locked Demo", locked: true } // pragma: allowlist secret (synthetic demo account)
    };

    var PROVIDERS = {
        "Cardiology": ["Dr. Avery Stone", "Dr. Priya Raman"],
        "Dermatology": ["Dr. Lena Ortiz"],
        "Family Medicine": ["Dr. Sam Whitaker", "Dr. Grace Liu", "Dr. Omar Haddad"],
        "Pediatrics": ["Dr. Nina Patel"]
    };

    var MAX_DAYS_AHEAD = 90;
    var PROVIDER_LATENCY_MS = 400;
    var LOGIN_LATENCY_MS = 300;
    var BOOKING_LATENCY_MS = 600;

    var KEY_USER = "portal.user";
    var KEY_APPTS = "portal.appointments";

    function $(id) { return document.getElementById(id); }

    function setError(field, message) {
        var el = $("error-" + field);
        if (el) { el.textContent = message || ""; }
    }

    function clearErrors(fields) {
        fields.forEach(function (f) { setError(f, ""); });
    }

    // ---------------------------------------------------------------- dates

    function today() {
        var d = new Date();
        d.setHours(0, 0, 0, 0);
        return d;
    }

    function addWeekdays(start, n) {
        var d = new Date(start);
        while (n > 0) {
            d.setDate(d.getDate() + 1);
            if (d.getDay() !== 0 && d.getDay() !== 6) { n--; }
        }
        return d;
    }

    function iso(d) {
        var m = String(d.getMonth() + 1).padStart(2, "0");
        var day = String(d.getDate()).padStart(2, "0");
        return d.getFullYear() + "-" + m + "-" + day;
    }

    function parseIso(value) {
        if (!/^\d{4}-\d{2}-\d{2}$/.test(value)) { return null; }
        var parts = value.split("-").map(Number);
        var d = new Date(parts[0], parts[1] - 1, parts[2]);
        if (d.getFullYear() !== parts[0] || d.getMonth() !== parts[1] - 1 || d.getDate() !== parts[2]) {
            return null;
        }
        return d;
    }

    // ---------------------------------------------------------------- storage

    function currentUser() {
        var raw = sessionStorage.getItem(KEY_USER);
        return raw ? JSON.parse(raw) : null;
    }

    function loadAppointments() {
        var raw = sessionStorage.getItem(KEY_APPTS);
        if (raw) { return JSON.parse(raw); }
        var seeded = [
            { id: "APT-100231", department: "Dermatology", provider: "Dr. Lena Ortiz",
              date: iso(addWeekdays(today(), 10)), time: "09:00", visitType: "Telehealth", status: "Scheduled" },
            { id: "APT-100232", department: "Cardiology", provider: "Dr. Avery Stone",
              date: iso(addWeekdays(today(), 15)), time: "13:30", visitType: "In person", status: "Scheduled" }
        ];
        saveAppointments(seeded);
        return seeded;
    }

    function saveAppointments(list) {
        sessionStorage.setItem(KEY_APPTS, JSON.stringify(list));
    }

    // ---------------------------------------------------------------- login page

    function initLogin() {
        var params = new URLSearchParams(window.location.search);
        var banner = $("login-error");
        if (params.get("reason") === "session") {
            banner.textContent = "Please sign in to continue.";
            banner.hidden = false;
        } else if (params.get("reason") === "signedout") {
            banner.textContent = "You have signed out.";
            banner.hidden = false;
        }

        $("login-form").addEventListener("submit", function (e) {
            e.preventDefault();
            banner.hidden = true;
            clearErrors(["username", "password"]);

            var username = $("username").value.trim();
            var secret = $("password").value;
            var valid = true;
            if (!username) { setError("username", "Username is required"); valid = false; }
            if (!secret) { setError("password", "Password is required"); valid = false; }
            if (!valid) { return; }

            $("sign-in").disabled = true;
            setTimeout(function () {
                $("sign-in").disabled = false;
                var user = USERS[username];
                if (user && user.locked) {
                    banner.textContent = "Your account is locked. Contact support.";
                    banner.hidden = false;
                    return;
                }
                if (!user || user.secret !== secret) {
                    banner.textContent = "Invalid username or password";
                    banner.hidden = false;
                    return;
                }
                sessionStorage.setItem(KEY_USER, JSON.stringify({ username: username, displayName: user.displayName }));
                window.location.href = "appointments.html";
            }, LOGIN_LATENCY_MS);
        });
    }

    // ---------------------------------------------------------------- appointments page

    function initAppointments() {
        var user = currentUser();
        if (!user) {
            window.location.replace("index.html?reason=session");
            return;
        }
        $("welcome").textContent = "Welcome, " + user.displayName;
        $("logout").addEventListener("click", function () {
            sessionStorage.clear();
            window.location.href = "index.html?reason=signedout";
        });

        var dept = $("department");
        Object.keys(PROVIDERS).forEach(function (name) {
            var opt = document.createElement("option");
            opt.value = name;
            opt.textContent = name;
            dept.appendChild(opt);
        });
        dept.addEventListener("change", onDepartmentChange);
        $("booking-form").addEventListener("submit", onBook);

        renderTable();
    }

    function onDepartmentChange() {
        var provider = $("provider");
        var department = $("department").value;
        provider.disabled = true;
        provider.innerHTML = "<option value=\"\">Select a provider</option>";
        if (!department) { return; }

        // Simulates an async lookup so tests must wait for the options explicitly.
        $("provider-loading").hidden = false;
        setTimeout(function () {
            PROVIDERS[department].forEach(function (name) {
                var opt = document.createElement("option");
                opt.value = name;
                opt.textContent = name;
                provider.appendChild(opt);
            });
            provider.disabled = false;
            $("provider-loading").hidden = true;
        }, PROVIDER_LATENCY_MS);
    }

    function validateBooking(form) {
        var fields = ["department", "provider", "date", "time", "phone", "reason", "consent"];
        clearErrors(fields);
        var ok = true;
        function fail(field, msg) { setError(field, msg); ok = false; }

        if (!form.department) { fail("department", "Please select a department"); }
        if (!form.provider) { fail("provider", "Please select a provider"); }

        if (!form.date) {
            fail("date", "Please enter a date");
        } else {
            var d = parseIso(form.date);
            if (!d) {
                fail("date", "Date must be in YYYY-MM-DD format");
            } else if (d <= today()) {
                fail("date", "Date must be in the future");
            } else if (Math.round((d - today()) / 86400000) > MAX_DAYS_AHEAD) {
                fail("date", "Appointments can be booked up to " + MAX_DAYS_AHEAD + " days ahead");
            } else if (d.getDay() === 0 || d.getDay() === 6) {
                fail("date", "The clinic is closed on weekends");
            }
        }

        if (!form.time) { fail("time", "Please select a time"); }

        if (!form.phone) {
            fail("phone", "Please enter a contact phone");
        } else if (!/^\d{3}-\d{3}-\d{4}$/.test(form.phone)) {
            fail("phone", "Phone must match 555-010-0000 format");
        }

        if (!form.reason) {
            fail("reason", "Please describe the reason for your visit");
        } else if (form.reason.length < 10) {
            fail("reason", "Reason must be at least 10 characters");
        }

        if (!form.consent) { fail("consent", "Please confirm to continue"); }

        if (ok) {
            var clash = loadAppointments().some(function (a) {
                return a.status === "Scheduled" && a.provider === form.provider
                    && a.date === form.date && a.time === form.time;
            });
            if (clash) { fail("time", "That time slot is already booked"); }
        }
        return ok;
    }

    function onBook(e) {
        e.preventDefault();
        $("confirmation").hidden = true;

        var form = {
            department: $("department").value,
            provider: $("provider").value,
            date: $("date").value.trim(),
            time: $("time").value,
            visitType: document.querySelector("input[name=visitType]:checked").value,
            phone: $("phone").value.trim(),
            reason: $("reason").value.trim(),
            consent: $("consent").checked
        };
        if (!validateBooking(form)) { return; }

        $("book").disabled = true;
        $("booking-spinner").hidden = false;
        setTimeout(function () {
            var list = loadAppointments();
            var id = "APT-" + (100300 + list.length);
            list.push({ id: id, department: form.department, provider: form.provider, date: form.date,
                        time: form.time, visitType: form.visitType, status: "Scheduled" });
            saveAppointments(list);

            $("booking-spinner").hidden = true;
            $("book").disabled = false;
            $("confirmation-number").textContent = id;
            $("confirmation").hidden = false;
            $("booking-form").reset();
            $("provider").disabled = true;
            renderTable();
        }, BOOKING_LATENCY_MS);
    }

    function renderTable() {
        var tbody = document.querySelector("#appointments-table tbody");
        tbody.innerHTML = "";
        loadAppointments()
            .slice()
            .sort(function (a, b) { return (a.date + a.time).localeCompare(b.date + b.time); })
            .forEach(function (a) {
                var tr = document.createElement("tr");
                tr.setAttribute("data-testid", "row-" + a.id);
                [a.id, a.department, a.provider, a.date, a.time, a.visitType, a.status].forEach(function (v) {
                    var td = document.createElement("td");
                    td.textContent = v;
                    tr.appendChild(td);
                });
                var action = document.createElement("td");
                if (a.status === "Scheduled") {
                    var btn = document.createElement("button");
                    btn.className = "secondary";
                    btn.textContent = "Cancel";
                    btn.setAttribute("data-testid", "cancel-" + a.id);
                    btn.addEventListener("click", function () { cancel(a.id); });
                    action.appendChild(btn);
                }
                tr.appendChild(action);
                tbody.appendChild(tr);
            });
    }

    function cancel(id) {
        var list = loadAppointments().map(function (a) {
            if (a.id === id) { a.status = "Cancelled"; }
            return a;
        });
        saveAppointments(list);
        renderTable();
    }

    window.Portal = { initLogin: initLogin, initAppointments: initAppointments };
})();
