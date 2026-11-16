# Mom & Pop Pizzeria — Java Swing Desktop Application (SWE 3313 Sprint 2)

**Team:** Group 5 (Alan Paccor, Nick Pettit, Juan Ramos Diaz, Amy Rice, Mustafa Sayed)  
A 100% standalone Java Swing desktop application (`javax.swing.*`, `java.awt.*`, `java.io.*`, `java.nio.file.*`) built for SWE 3313 Sprint 2. Requires **zero external third-party libraries**, zero internet access, and zero database server setup.

---

## Pre-Configured Logins & Sample Data

On first launch, the app automatically reads/creates plain-text database files inside `data/`:
- `data/staff.txt`
- `data/customers.txt`
- `data/orders.txt`

### Staff Portal Credentials (`data/staff.txt`) — Password Protected
| Role | Username | Password |
| :--- | :--- | :--- |
| **Owner** | `owner` | `pizza123` |
| **Manager** | `manager1` | `pizza123` |
| **Worker** | `worker1` | `pizza123` |

### Sample Returning Customer Phone Numbers (`data/customers.txt`)
- `555-555-5555` (`Jane Doe` — Delivery to `123 Oak Street`, Charge Account: `Visa`)
- `124-183-7581` (`James Smith` — Delivery to `555 Freemont Circle`, Charge Account: `MasterCard`)
- `555-123-4567` (`Juan Ramos` — Delivery to `105 Ardenlee Dr`, Charge Account: `Visa`)

---

## SWE 3313 Sprint 2 Compliance Checklist
1. **Standalone Desktop GUI:** Uses only standard JDK `javax.swing.*`, `java.awt.*`, `java.io.*`, and `java.nio.file.*` — zero web, HTTP, or Supabase calls.
2. **File-Based Simulated Database (`DataStore.java`):** Stores staff, customer master records (including phone, address, subdivision, nearest major intersection, and charge account type `Visa`/`MasterCard`), and completed orders in `data/*.txt`.
3. **Complete Menu & Builder (`MenuOrderPanel.java`):**
   - **4 Pizza Sizes:** Small (10") `$10.99`, Medium (12") `$13.99`, Large (14") `$16.99`, X-Large (16") `$19.99`.
   - **4 Crust Options:** Classic Hand-Tossed, Thin & Crispy, Stone-Baked Thick (`+$1.50`), Garlic Butter Herb (`+$1.00`).
   - **3 Sauces:** Light Sauce, Normal Sauce, Extra Sauce.
   - **13 Distinct Toppings (7 Veggie/Cheese incl. Extra Mozzarella Cheese + 6 Meats):** Strictly enforces a **maximum of 4 toppings per pizza**.
   - **6 Beverages across 3 Distinct Sizes:** Small (12 oz), Medium (20 oz `+$0.75`), Large (32 oz `+$1.50`).
   - Live item pricing and interactive Order Cart (Add, Edit Pizza, Remove, Clear).
4. **All 3 Required Reports (`OrderRecord.java`, `DataStore.java`):**
   - **Customer Receipt:** Itemized receipt with fulfillment type, delivery fee (`$3.99`), 8% sales tax, tip, payment method/amount, and signature line.
   - **Cook Order Slip:** Kitchen preparation ticket with item prep checklist.
   - **Daily Business Summary Report:** Management summary showing order volume, tender totals (Credit, Debit, Cash, Check), taxes, tips, and gross revenue.
5. **Password-Protected Staff Terminal (`PizzaAppFrame.java`, `AdminPanel.java`):**
   - Requires staff authentication (`owner` / `pizza123`) before viewing Transactions, Customer Directory, Staff Directory, or Daily Summary.
   - Includes `Lock Terminal` button and password masking toggle.
6. **Javadoc Documentation:** 100% Javadoc coverage verified with `javadoc -Xdoclint:all -private`.

---

## How to Compile, Run & Package

### macOS / Linux
```bash
cd sprint2-java-swing
chmod +x compile-and-run.sh
./compile-and-run.sh
```

Run automated headless verification:
```bash
./compile-and-run.sh --smoke-test
```

### Windows (Compile, Run, and Build `.exe`)
1. Double-click or run `build-exe.bat` (requires JDK 17+ and WiX Toolset v3+ to produce the native `.exe` installer via `jpackage`).
2. Or double-click / launch the runnable JAR `build/jar/MomAndPopPizza.jar`:
```bat
java -jar build\jar\MomAndPopPizza.jar
```

### Deliverable Archive
`MomAndPopPizza-Sprint2.zip` packages the entire `sprint2-java-swing` project directory (`src/`, `data/`, `build/jar/MomAndPopPizza.jar`, `compile-and-run.sh`, `build-exe.bat`, and `README.md`).
