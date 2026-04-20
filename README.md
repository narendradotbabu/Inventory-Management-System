# RetailPOS — Inventory & Billing System
**UE23CS352B — OOAD Mini Project | PES University | Jan–May 2026**

Team: Manoj R · Narendra Babu · Rohan A · Kotte Kamal

---

## Tech Stack
| Layer | Technology |
|-------|-----------|
| UI | JavaFX 21 + FXML |
| Language | Java 21 |
| Database | SQLite (auto-created on first run) |
| Build | Maven 3.9+ |
| Patterns | MVC, Factory, Strategy, Singleton |

---

## Prerequisites

Install these before running:

1. **JDK 21** — [Download from Adoptium](https://adoptium.net/)
   - Verify: `java -version` (must show 21.x)

2. **Maven 3.9+** — [Download Maven](https://maven.apache.org/download.cgi)
   - Verify: `mvn -version`

3. **VS Code** with these extensions:
   - Extension Pack for Java (Microsoft)
   - Maven for Java

---

## Project Structure

```
retailpos/
├── pom.xml                          ← Maven build config
├── README.md
├── src/
│   └── main/
│       ├── java/
│       │   ├── module-info.java
│       │   └── com/retailpos/
│       │       ├── MainApp.java              ← Entry point
│       │       ├── controller/
│       │       │   ├── LoginController.java
│       │       │   ├── MainController.java
│       │       │   ├── POSController.java
│       │       │   └── InventoryController.java
│       │       ├── dao/
│       │       │   ├── UserDAO.java
│       │       │   ├── ProductDAO.java
│       │       │   ├── SaleDAO.java
│       │       │   └── ReturnDAO.java
│       │       ├── factory/
│       │       │   └── PaymentFactory.java
│       │       ├── model/
│       │       │   ├── User.java
│       │       │   ├── Product.java
│       │       │   ├── Sale.java / SaleItem.java
│       │       │   ├── Payment.java (abstract)
│       │       │   ├── CashPayment.java
│       │       │   ├── OnlinePayment.java
│       │       │   └── ReturnItem.java
│       │       └── util/
│       │           └── DatabaseHelper.java   ← SQLite Singleton
│       └── resources/
│           ├── fxml/                         ← All UI screens
│           └── css/styles.css
└── .vscode/
    ├── settings.json
    └── launch.json
```

---

## ▶ How to Run

### Option 1 — Maven (Recommended, works everywhere)

```bash
# 1. Open terminal in the retailpos/ folder
cd retailpos

# 2. Download dependencies & compile
mvn clean compile

# 3. Run the app
mvn javafx:run
```

That's it! The SQLite database (`retailpos.db`) is auto-created on first run.

---

### Option 2 — VS Code GUI

1. Open VS Code → **File → Open Folder** → select the `retailpos/` folder
2. Wait for Java extension to index the project (bottom-right progress bar)
3. Open `src/main/java/com/retailpos/MainApp.java`
4. Click the **▷ Run** button above the `main()` method
   - OR press `F5` (uses `.vscode/launch.json`)

> **If you get a JavaFX module error in VS Code:** Run via Maven terminal instead (`mvn javafx:run`) — this is the most reliable method.

---

### Option 3 — Build a runnable JAR

```bash
mvn clean package
java -jar target/retailpos-1.0.0.jar
```

---

## Default Login Credentials

| Role | Username | Password |
|------|----------|----------|
| Admin | `admin` | `admin123` |
| Cashier | `cashier1` | `cashier123` |

*(Created automatically on first run by DatabaseHelper)*

---

## Features
- 🔐 Role-based login (Admin / Cashier)
- 🛒 POS — barcode scan, item selection, auto tax calculation
- 💳 Cash & Online payments (UPI/Card/Net Banking)
- 🔄 Returns & Exchanges with refund processing
- 📦 Inventory management (Admin only)
- 👥 User management (Admin only)
- 🧾 Receipt generation & printing

---

## Troubleshooting

| Problem | Fix |
|---------|-----|
| `java.lang.UnsupportedClassVersionError` | Install JDK 21, not JRE |
| `mvn: command not found` | Add Maven `bin/` to your PATH |
| Blank white screen on launch | Run `mvn clean compile` first, then `mvn javafx:run` |
| DB errors on first run | Delete `retailpos.db` and re-run to regenerate |
