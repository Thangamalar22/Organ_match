import sys
sys.stdout.reconfigure(encoding='utf-8')
from playwright.sync_api import sync_playwright

with sync_playwright() as p:
    browser = p.chromium.launch(headless=True)
    page = browser.new_page()
    page.goto('http://localhost:8085/login')
    page.fill('input[name="username"]', 'admin')
    page.fill('input[name="password"]', 'Admin@123')
    page.click('button[type="submit"]')
    page.wait_for_load_state('networkidle')

    page.goto('http://localhost:8085/match')
    print('Match page content:')
    print(page.content())
    browser.close()
