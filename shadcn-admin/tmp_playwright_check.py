from playwright.sync_api import sync_playwright

BASE_URL = 'http://127.0.0.1:4173'

with sync_playwright() as p:
    browser = p.chromium.launch(headless=True)
    page = browser.new_page(viewport={"width": 1440, "height": 960})

    results = []

    def record(name, value):
        results.append(f"{name}: {value}")

    page.goto(BASE_URL, wait_until='networkidle')
    record('home_url', page.url)
    record('home_title', page.title())
    record('has_sidebar', page.locator('[data-slot="sidebar"]').count() > 0)
    record('has_config_drawer_trigger', page.get_by_role('button', name='Open theme settings').count() > 0)

    html_class = page.locator('html').get_attribute('class') or ''
    record('html_class_initial', html_class)

    page.get_by_role('button', name='Open theme settings').click()
    page.wait_for_timeout(500)
    record('drawer_title_visible', page.get_by_text('Theme Settings').is_visible())

    page.get_by_label('Select theme preference').get_by_label('Dark').click()
    page.wait_for_timeout(500)
    dark_class = page.locator('html').get_attribute('class') or ''
    record('html_class_after_dark', dark_class)

    page.keyboard.press('Control+k')
    page.wait_for_timeout(500)
    record('command_dialog_visible', page.get_by_role('dialog').count() > 0)
    page.keyboard.press('Escape')
    page.wait_for_timeout(300)

    page.goto(f'{BASE_URL}/sign-in', wait_until='networkidle')
    record('signin_url', page.url)
    record('signin_has_email_or_username', page.locator('input').count() > 0)

    response = page.goto(f'{BASE_URL}/images/favicon.svg', wait_until='networkidle')
    record('favicon_status', response.status if response else 'no-response')

    browser.close()

print('\n'.join(results))
