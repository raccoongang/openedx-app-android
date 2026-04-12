#!/usr/bin/env python3
"""Appium E2E test: Registration flow using EditText field indices."""
import time, sys, random, string
from appium import webdriver
from appium.options.android import UiAutomator2Options
from appium.webdriver.common.appiumby import AppiumBy
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.common.exceptions import TimeoutException, NoSuchElementException

APPIUM_SERVER = "http://127.0.0.1:4723"
APK_PATH = "/Users/paul/StudioProjects/openedx-app-cmp/app/build/outputs/apk/develop/debug/app-develop-debug.apk"

suffix = ''.join(random.choices(string.ascii_lowercase + string.digits, k=6))
TEST_NAME = f"Test User {suffix}"
TEST_USERNAME = f"tuser{suffix}"
TEST_EMAIL = f"tuser{suffix}@example.com"
TEST_PASSWORD = "TestPass123!"


def log(msg):
    print(f"[TEST] {msg}", flush=True)


def find_by_text(driver, text, timeout=15):
    return WebDriverWait(driver, timeout).until(
        EC.presence_of_element_located(
            (AppiumBy.ANDROID_UIAUTOMATOR, f'new UiSelector().text("{text}")')
        )
    )


def scroll_down(driver):
    size = driver.get_window_size()
    driver.swipe(size['width']//2, int(size['height']*0.7), size['width']//2, int(size['height']*0.3), 600)
    time.sleep(0.5)


def get_edit_texts(driver):
    return driver.find_elements(AppiumBy.CLASS_NAME, "android.widget.EditText")


def fill_field(driver, index, value, label=""):
    """Fill EditText field by index, scrolling if needed."""
    fields = get_edit_texts(driver)
    if index < len(fields):
        fields[index].click()
        time.sleep(0.3)
        fields[index].clear()
        fields[index].send_keys(value)
        log(f"  Filled field[{index}] ({label}): {value}")
        # Hide keyboard
        try:
            driver.hide_keyboard()
        except Exception:
            pass
        return True
    return False


def main():
    log(f"Registration test — user: {TEST_EMAIL}")

    options = UiAutomator2Options()
    options.app = APK_PATH
    options.device_name = "Android Emulator"
    options.automation_name = "UiAutomator2"
    options.no_reset = False
    options.new_command_timeout = 300
    options.set_capability("appium:forceAppLaunch", True)
    options.set_capability("appium:shouldTerminateApp", True)

    driver = webdriver.Remote(APPIUM_SERVER, options=options)
    log("App launched.")

    try:
        time.sleep(5)

        # Step 1: Navigate to Sign Up
        log("Step 1: Click 'Register' to navigate to Sign Up...")
        register_link = find_by_text(driver, "Register", timeout=15)
        register_link.click()
        log("  Clicked Register")

        # Step 2: Wait for form to load
        log("Step 2: Waiting for registration form...")
        for attempt in range(12):
            time.sleep(3)
            fields = get_edit_texts(driver)
            if fields:
                log(f"  Form loaded with {len(fields)} fields after {(attempt+1)*3}s")
                break
        else:
            log("  FAIL: Form did not load after 36s")
            sys.exit(1)

        # Step 3: Fill form fields
        # The registration form typically has: name, username, email, password
        # Fields may need scrolling to become visible
        log("Step 3: Filling registration form...")

        # Fill name (should be first field)
        fill_field(driver, 0, TEST_NAME, "name")
        time.sleep(0.5)

        # Fill username (second field)
        fields = get_edit_texts(driver)
        if len(fields) > 1:
            fill_field(driver, 1, TEST_USERNAME, "username")
        else:
            scroll_down(driver)
            fill_field(driver, 0, TEST_USERNAME, "username after scroll")
        time.sleep(0.5)

        # Fill email
        fields = get_edit_texts(driver)
        if len(fields) > 2:
            fill_field(driver, 2, TEST_EMAIL, "email")
        else:
            scroll_down(driver)
            time.sleep(0.5)
            fields = get_edit_texts(driver)
            for i, f in enumerate(fields):
                txt = f.get_attribute("text") or ""
                if not txt or "email" in txt.lower() or "enter" in txt.lower():
                    fill_field(driver, i, TEST_EMAIL, "email")
                    break
        time.sleep(0.5)

        # Fill password — likely need to scroll down
        scroll_down(driver)
        time.sleep(0.5)
        fields = get_edit_texts(driver)
        # Find the password field (usually last visible, or has password hint)
        password_filled = False
        for i, f in enumerate(fields):
            txt = f.get_attribute("text") or ""
            is_password = f.get_attribute("password") == "true"
            if is_password or "password" in txt.lower() or "enter password" in txt.lower():
                fill_field(driver, i, TEST_PASSWORD, "password")
                password_filled = True
                break
        if not password_filled:
            # Try last field
            if fields:
                fill_field(driver, len(fields)-1, TEST_PASSWORD, "password (last field)")
        time.sleep(0.5)

        # Step 4: Click Create Account
        log("Step 4: Clicking Create Account...")
        scroll_down(driver)
        time.sleep(1)
        scroll_down(driver)
        time.sleep(1)

        try:
            create_btn = find_by_text(driver, "Create account", timeout=10)
            create_btn.click()
            log("  Clicked Create Account")
        except TimeoutException:
            log("  Create Account button not found!")
            sys.exit(1)

        # Step 5: Wait for result (registration + login = 2+ API calls)
        log("Step 5: Waiting for navigation (up to 30s)...")
        time.sleep(30)

        # Check for errors on the screen
        try:
            elements = driver.find_elements(AppiumBy.XPATH, "//*[@text!='']")
            texts = [el.get_attribute("text") for el in elements if el.get_attribute("text")]
            log(f"  Screen texts: {texts[:10]}")
        except Exception:
            pass

        # Check for main screen (Learn/Discover/Profile tabs)
        success = False
        for tab in ["Learn", "Discover", "Profile"]:
            try:
                find_by_text(driver, tab, timeout=10)
                log(f"  SUCCESS: Found '{tab}' tab — on main screen!")
                success = True
                break
            except TimeoutException:
                continue

        if not success:
            # Maybe there's a WhatsNew screen or other intermediary
            try:
                # Check for "What's New" or "Done" or "Get Started"
                for btn_text in ["Done", "Get Started", "Skip", "Continue", "Close"]:
                    try:
                        btn = find_by_text(driver, btn_text, timeout=3)
                        log(f"  Found intermediary button: '{btn_text}', clicking...")
                        btn.click()
                        time.sleep(3)
                        # Check again for main screen
                        for tab in ["Learn", "Discover", "Profile"]:
                            try:
                                find_by_text(driver, tab, timeout=5)
                                log(f"  SUCCESS: Found '{tab}' after intermediary")
                                success = True
                                break
                            except TimeoutException:
                                continue
                        if success:
                            break
                    except TimeoutException:
                        continue
            except Exception:
                pass

        if success:
            log("=" * 50)
            log("REGISTRATION TEST PASSED")
            log("=" * 50)
        else:
            log("=" * 50)
            log("REGISTRATION TEST FAILED")
            log("=" * 50)
            sys.exit(1)

    except Exception as e:
        log(f"ERROR: {e}")
        import traceback; traceback.print_exc()
        sys.exit(1)
    finally:
        driver.quit()


if __name__ == "__main__":
    main()
