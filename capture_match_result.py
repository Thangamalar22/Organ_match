import time
from playwright.sync_api import sync_playwright

with sync_playwright() as p:
    browser = p.chromium.launch(headless=True)
    page = browser.new_page(viewport={'width': 1280, 'height': 1000}, device_scale_factor=2)
    page.goto('http://localhost:8085/login')
    page.fill('input[name="username"]', 'admin')
    page.fill('input[name="password"]', 'Admin@123')
    page.click('button[type="submit"]')
    page.wait_for_load_state('networkidle')

    page.goto('http://localhost:8085/match')
    page.wait_for_timeout(1000)

    # Trigger CSP matching execution
    page.evaluate("runMatching('CSP')")
    page.wait_for_timeout(1500)
    page.screenshot(path='report_images/07_match_results_csp.png')
    print('Captured 07_match_results_csp.png')

    # Trigger Compare matching execution (CSP vs Greedy)
    page.evaluate("runMatching('COMPARE')")
    page.wait_for_timeout(1500)
    page.screenshot(path='report_images/07_match_results_compare.png')
    print('Captured 07_match_results_compare.png')

    browser.close()
