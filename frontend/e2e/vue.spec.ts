import { test, expect } from '@playwright/test'

test('renders the catalog without waiting on the backend', async ({ page }) => {
  // Simulate a cold-starting backend: the request hangs and never resolves.
  await page.route('**/api/activities', () => {
    /* deliberately never fulfilled */
  })

  await page.goto('/')

  await expect(page.locator('h1')).toHaveText('AI Travel Concierge')
  // The grid must be populated from the bundled snapshot, not blocked on the API.
  await expect(page.locator('article.card')).toHaveCount(10, { timeout: 3000 })
})

test('filters experiences as you type', async ({ page }) => {
  await page.goto('/')

  await page.getByRole('searchbox').fill('kayak')

  await expect(page.locator('article.card')).toHaveCount(1)
  await expect(page.locator('article.card h3')).toHaveText('Kayak Tour on the Spree River')
})

test('opens the concierge widget', async ({ page }) => {
  await page.goto('/')

  await page.getByRole('button', { name: 'Open the AI concierge' }).click()

  await expect(page.getByRole('dialog', { name: 'AI Travel Concierge' })).toBeVisible()
})
