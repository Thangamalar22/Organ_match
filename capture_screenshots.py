import os
import time
from playwright.sync_api import sync_playwright

os.makedirs('report_images', exist_ok=True)

with sync_playwright() as p:
    browser = p.chromium.launch(headless=True)
    context = browser.new_context(viewport={'width': 1280, 'height': 800}, device_scale_factor=2)
    page = context.new_page()

    # 1. Login Page
    print('Navigating to Login...')
    page.goto('http://localhost:8085/login')
    page.screenshot(path='report_images/01_login.png')
    print('Captured 01_login.png')

    # Perform Login
    page.fill('input[name="username"]', 'admin')
    page.fill('input[name="password"]', 'Admin@123')
    page.click('button[type="submit"]')
    page.wait_for_load_state('networkidle')

    # 2. Dashboard
    print('Navigating to Dashboard...')
    page.goto('http://localhost:8085/dashboard')
    page.screenshot(path='report_images/02_dashboard.png')
    print('Captured 02_dashboard.png')

    # 3. Recipients List
    print('Navigating to Recipients...')
    page.goto('http://localhost:8085/recipients')
    page.screenshot(path='report_images/03_recipients_list.png')
    print('Captured 03_recipients_list.png')

    # 4. New Recipient Form
    print('Navigating to New Recipient Form...')
    page.goto('http://localhost:8085/recipients/new')
    page.screenshot(path='report_images/04_recipient_new.png')
    print('Captured 04_recipient_new.png')

    # 5. Donors List
    print('Navigating to Donors...')
    page.goto('http://localhost:8085/donors')
    page.screenshot(path='report_images/05_donors_list.png')
    print('Captured 05_donors_list.png')

    # 6. Match Execution Page
    print('Navigating to Match Page...')
    page.goto('http://localhost:8085/match')
    page.screenshot(path='report_images/06_match_page.png')
    print('Captured 06_match_page.png')

    # Perform Matching Execution
    select_el = page.query_selector('select[name="donorId"]')
    if select_el:
        select_el.select_option(index=1)
        page.click('button[type="submit"]')
        page.wait_for_load_state('networkidle')
        page.screenshot(path='report_images/07_match_results.png')
        print('Captured 07_match_results.png')

    # 8. Match History Audit Log
    print('Navigating to History...')
    page.goto('http://localhost:8085/history')
    page.screenshot(path='report_images/08_history.png')
    print('Captured 08_history.png')

    # 9. History Detail View
    detail_link = page.query_selector('a[href*="/history/"]')
    if detail_link:
        detail_link.click()
        page.wait_for_load_state('networkidle')
        page.screenshot(path='report_images/09_history_detail.png')
        print('Captured 09_history_detail.png')

    # 10. Benchmark Experiments Page
    print('Navigating to Experiments...')
    page.goto('http://localhost:8085/experiments')
    page.screenshot(path='report_images/10_experiments.png')
    print('Captured 10_experiments.png')

    # Run Benchmark Experiment
    exp_btn = page.query_selector('button[type="submit"]')
    if exp_btn:
        exp_btn.click()
        page.wait_for_load_state('networkidle')
        page.screenshot(path='report_images/10_experiments_results.png')
        print('Captured 10_experiments_results.png')

    # 11. Admin User Management
    print('Navigating to Admin Users...')
    page.goto('http://localhost:8085/admin/users')
    page.screenshot(path='report_images/11_admin_users.png')
    print('Captured 11_admin_users.png')

    # 12. User Profile
    print('Navigating to Profile...')
    page.goto('http://localhost:8085/profile')
    page.screenshot(path='report_images/12_profile.png')
    print('Captured 12_profile.png')

    browser.close()
    print('All screenshots captured successfully!')
