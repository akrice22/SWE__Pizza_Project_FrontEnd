# Mom & Pop Pizzeria — Java Swing Desktop Application (SWE 3313 Sprint 2)

A 100% standalone Java Swing desktop application (`javax.swing.*`, `java.awt.*`, `java.io.*`, `java.nio.file.*`) migrated from the React frontend for SWE 3313 Sprint 2. Requires **zero external third-party libraries**, zero internet access, and zero database server setup.

---

## Pre-Configured Logins & Sample Data

On first launch, the app automatically initializes local plain-text files inside `data/`:
- `data/staff.txt`
- `data/customers.txt`
- `data/orders.txt`

### Staff Portal Credentials (`data/staff.txt`)
| Role | Username | Password |
| :--- | :--- | :--- |
| **Owner** | `owner` | `pizza123` |
| **Manager** | `manager1` | `pizza123` |
| **Worker** | `worker1` | `pizza123` |

### Sample Returning Customer Phone Numbers (`data/customers.txt`)
- `555-555-5555` (`Jane Doe` — Delivery to `123 Oak Street`)
- `124-183-7581` (`James Smith` — Delivery to `555 Freemont Circle`)
- `555-123-4567` (`Juan Ramos` — Delivery to `105 Ardenlee Dr`)

---

## Project Structure

```text
sprint2-java-swing/
├── README.md
├── compile-and-run.sh          # macOS / Linux compile, JAR build, and launch script
├── build-exe.bat               # Windows compile, JAR build, and jpackage .exe script
├── data/                       # Auto-generated plain-text database files
│   ├── staff.txt
│   ├── customers.txt
│   └── orders.txt
└── src/com/momandpop/pizza/
    ├── MomAndPopPizzaApp.java  # Main entry point + headless --smoke-test
    ├── model/
    │   ├── Customer.java
    │   ├── MenuCatalog.java    # 4 sizes, 4 crusts, 3 sauces, 12 toppings, 6 drinks x 3 sizes
    │   ├── OrderItem.java
    │   ├── OrderRecord.java
    │   └── StaffUser.java
    ├── storage/
    │   └── DataStore.java      # Local file I/O (staff.txt, customers.txt, orders.txt)
    └── ui/
        ├── Theme.java
        ├── PizzaAppFrame.java
        ├── LoginPanel.java
        ├── CustomerPanel.java
        ├── MenuOrderPanel.java
        ├── CheckoutReceiptPanel.java
        ├── SignatureCanvasPanel.java
        └── AdminPanel.java
```

---

## Implemented Sprint 2 Requirements
1. **Login Screen (`LoginPanel`):** Validates staff username/password against `data/staff.txt`, plus quick-start customer ordering and phone lookup.
2. **Customer Lookup & Entry (`CustomerPanel`):** 10-digit phone lookup auto-filling returning customer fields and past order history from `customers.txt` / `orders.txt`, plus Delivery ($3.99 fee) vs. Pickup ($0.00 fee) selection.
3. **Menu & Pizza Builder (`MenuOrderPanel`):**
   - **4 Pizza Sizes:** Small (10") `$10.99`, Medium (12") `$13.99`, Large (14") `$16.99`, X-Large (16") `$19.99`.
   - **4 Crusts:** Classic Hand-Tossed, Thin & Crispy, Stone-Baked Thick (`+$1.50`), Garlic Butter Herb (`+$1.00`).
   - **3 Sauces:** Light Sauce, Normal Sauce, Extra Sauce.
   - **12 Distinct Toppings (6 Veggies + 6 Meats):** Enforces a strict **maximum of 4 toppings per pizza** with immediate checkbox rollback and warning popup.
   - **6 Beverages across 3 Sizes:** Small (12 oz), Medium (20 oz `+$0.75`), Large (32 oz `+$1.50`).
   - Live item price calculation and an interactive cart with Edit, Remove, and Clear controls.
4. **Checkout & Receipt (`CheckoutReceiptPanel`):**
   - Payment methods: Credit, Debit, Cash (with change due), and Check (check # + check amount).
   - Tip presets (`0%`, `15%`, `18%`, `20%`, Custom `$`), 8% sales tax, and $3.99 delivery fee.
   - Typed and mouse-drawn (`SignatureCanvasPanel`) signature authorization.
   - Generates a formatted itemized thermal-style receipt and writes the completed order to `data/orders.txt`.
5. **Staff / Admin View (`AdminPanel`):** Tabular view of `orders.txt`, `customers.txt`, and `staff.txt`, plus gross revenue summary, receipt viewer, and new staff enrollment.

---

## How to Compile and Run

### macOS / Linux
```bash
cd sprint2-java-swing
chmod +x compile-and-run.sh
./compile-and-run.sh
```

To run the automated headless verification test:
```bash
./compile-and-run.sh --smoke-test
```

### Windows (Compile & Package `.exe`)
Ensure JDK 17+ (including `javac`, `jar`, and `jpackage`) is installed. To package a `.exe` installer with `jpackage`, install the WiX Toolset (v3+). Then run:
```bat
cd sprint2-java-swing
build-exe.bat
```
Or run directly without building an installer:
```bat
javac -d build\classes src\com\momandpop\pizza\*.java src\com\momandpop\pizza\model\*.java src\com\momandpop\pizza\storage\*.java src\com\momandpop\pizza\ui\*.java
java -cp build\classes com.momandpop.pizza.MomAndPopPizzaApp
```
