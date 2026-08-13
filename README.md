# Matcha Box Cafe System

Spring Boot + MySQL + HTML/CSS/JavaScript cafe ordering/POS system.

## Run in IntelliJ IDEA

1. Install JDK 17 or newer.
2. Install MySQL and make sure the MySQL server is running.
3. Open this folder in IntelliJ IDEA as a Maven project.
4. Open `matchabox_schema.sql` in MySQL Workbench/phpMyAdmin and run it. This creates the `matchabox` database and sample data.
5. Set your MySQL root password:
   - easiest: edit `src/main/resources/application.properties` and replace `${DB_PASSWORD:}` with your password, or
   - set an IntelliJ environment variable named `DB_PASSWORD`.
6. Run `com.matchabox.MatchaBoxApplication`.
7. Open `http://localhost:8080/` — it redirects to the customer menu.

## Main pages

- Customer menu: `/menu.html`
- Checkout: `/checkout.html`
- Payment: `/payment.html`
- Staff login: `/staff-login.html`
- POS: `/pos.html`
- Clock in/out: `/clock.html`

## Important

If you previously created an older `matchabox` database, back it up first. For a clean test, drop the old database and run the supplied schema again so the table/column definitions match the fixed Java entities.
