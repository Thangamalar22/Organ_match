# Multi-Organ Donor-Recipient Matching using a Constraint Satisfaction Problem (CSP)

> **Data is synthetic. Decision-support simulation only.**

A Spring Boot 3 (Java 17+, Maven) academic project demonstrating the application of Constraint Satisfaction Problem (CSP) algorithms to multi-organ donor-recipient matching. The core CSP engine is completely hand-written without external optimization libraries (such as Choco or OR-Tools).

---

## 📌 Project Overview

Organ allocation is a time-critical, multi-criteria resource allocation problem. A single brain-dead donor can donate multiple organs (kidneys, liver, heart, lungs, pancreas). Allocating organs individually without global coordination leads to sub-optimal outcomes and potential waste.

This project formulates multi-organ allocation as a CSP:
- **Hand-written CSP Engine**: Includes node consistency, backtracking, MRV, degree tie-breaker, LCV value ordering, forward checking with early wipe-out detection, UNASSIGNED handling, and Branch & Bound pruning.
- **Decision-Support Focus**: Provides human-readable explanations of why candidate recipients were selected or eliminated.
- **Role-Based Access Control & Audit Logging**: Multi-hospital data isolation, role-gated matching execution, audit history logging, and CSV exports.

---

## 🔐 Roles & Permissions Matrix

| Feature / Page | `ADMIN` | `COORDINATOR` | `HOSPITAL` |
| :--- | :---: | :---: | :---: |
| **System Dashboard (`/dashboard`)** | Full System Stats | System Utility Metrics | Hospital Portal Summary |
| **Recipients Management (`/recipients`)** | Full View & CRUD | Read-Only (All Hospitals) | View & CRUD (Own Hospital Only) |
| **Donors Management (`/donors`)** | Full View & CRUD | Read-Only (All Hospitals) | View & CRUD (Own Hospital Only) |
| **Run Matching (`/match`)** | Execute & Save | Execute & Save | ❌ Forbidden |
| **Match History (`/history`, `/history/{id}`)** | View, Print, Export CSV | View, Print, Export CSV | ❌ Forbidden |
| **Benchmark Experiments (`/experiments`)** | Run & Export CSV | Run & Export CSV | ❌ Forbidden |
| **User Administration (`/admin/users`)** | Full User CRUD | ❌ Forbidden | ❌ Forbidden |
| **Profile Settings (`/profile`)** | Change Password | Change Password | Change Password |

---

## 🔑 Seeded Demo Accounts

The system automatically seeds demo accounts on first launch (Password policy: see below):

| Username | Role | Hospital Affiliation | Password |
| :--- | :--- | :--- | :--- |
| `admin` | `ADMIN` | System Wide | `Admin@123` |
| `coord1` | `COORDINATOR` | National Coordinating Center | `Coord@123` |
| `hospital1` | `HOSPITAL` | Apollo Chennai | `Hosp@123` |
| `hospital2` | `HOSPITAL` | AIIMS Delhi | `Hosp@123` |

---

## 🌐 Application Pages & Routes

- **`/login`**: Public authentication page with demo credentials helper.
- **`/dashboard`**: Role-tailored operational overview and recent activity summaries.
- **`/recipients` & `/recipients/new` & `/recipients/{id}/edit`**: Recipient waiting list management with pagination, filtering, and hospital ownership guards.
- **`/donors` & `/donors/new` & `/donors/{id}/edit`**: Donor registration and management with kidney counts and available organ checkboxes.
- **`/match`**: Stored donor selection, random donor generation, CSP configuration flags, matching execution, and live result visualization.
- **`/history` & `/history/{id}`**: Historical audit log of all matching runs with user/algorithm filters, CSV export, and print-styled detailed reports.
- **`/experiments`**: Automated benchmark suite evaluating solver heuristics (A, B, C, D vs Greedy) over stored database recipients, with CSV export.
- **`/admin/users` & `/admin/users/new`**: Administrator user management (status toggling, password reset, account deletion with self-protection guards).
- **`/profile`**: Account information view and self-service password update.

---

## 🧩 CSP Problem Formulation

### 1. Variables ($X$)
Each available organ unit from a deceased donor is modeled as a discrete CSP variable $V_i$:
- Example: `D101-KIDNEY-1`, `D101-KIDNEY-2`, `D101-HEART-1`, `D101-LIVER-1`.

### 2. Domains ($D$)
The domain $D(V_i)$ for variable $V_i$ consists of candidate recipients needing that organ type, plus `null` (representing `UNASSIGNED`).

### 3. Hard Constraints ($C$)

#### Unary / Node Constraints (Node Consistency)
1. **Organ Type Match**: Recipient's needed organ must equal organ type.
2. **ABO Blood Compatibility**: Donor blood group must be compatible with recipient blood group.
3. **Medical Fitness**: `recipient.medicallyFit` must be `true`.
4. **Size Compatibility**:
   - `HEART` & `LUNGS`: Donor weight / Recipient weight ratio $\in [0.8, 1.2]$.
   - `LIVER`: Ratio $\in [0.7, 1.5]$.
   - `KIDNEY` & `PANCREAS`: Skipped.
5. **Cold Ischemia Time**: Transport transit time $+ 1.5\text{h}$ prep buffer $\le$ maximum allowed cold ischemia hours.
6. **Crossmatch Test (Kidney)**: `recipient.crossmatchPositive` must be `false`.
7. **HLA Antigen Match (Kidney)**: At least 3 of 6 HLA antigens must match by position.
8. **Pediatric Match**: Organs from donors under age 18 are reserved for pediatric recipients (age $< 18$).

#### Global Search Constraints
9. **One Organ Per Recipient**: A recipient can receive at most one organ in a single donor allocation run.

### 4. Objective & Scoring Function
Maximizes total utility score $U(V, R) \in [0.0, 1.0]$:
$$U(V, R) = 0.40 \cdot \text{Urgency} + 0.20 \cdot \text{WaitingDays} + 0.20 \cdot \text{HLA} + 0.10 \cdot \text{AgeBenefit} + 0.10 \cdot \text{Transport}$$

---

## 🚀 How to Run

### Prerequisites
- JDK 17+
- Maven wrapper (included)

### Running the Application
```bash
# Windows
.\mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```

Access the application web UI at: **`http://localhost:8080/`**  

### Running Unit & Integration Tests
```bash
# Windows
.\mvnw.cmd clean test

# Linux / macOS
./mvnw clean test
```

### 🔄 Resetting the Database
The H2 file-based database is saved at `./data/organmatch.mv.db`. To reset the database to its initial seeded state:
1. Stop the running application.
2. Delete the `./data` folder in the project root directory:
   ```bash
   # Windows
   Remove-Item -Recurse -Force ./data

   # Linux / macOS
   rm -rf ./data
   ```
3. Restart the application. The `DataSeeder` will automatically recreate the database schema and populate seed users and synthetic candidates.

---

## ⚠️ Production Security Notice

> [!IMPORTANT]
> The H2 Web Console is enabled for local development at `http://localhost:8080/h2-console` (connected to `jdbc:h2:file:./data/organmatch`).
> **For production deployments, the H2 console must be explicitly disabled** in `application.properties`:
> ```properties
> spring.h2.console.enabled=false
> ```

---

## ⚠️ Limitations & Scope

- **Data Source**: Synthetic generated data via seeded `java.util.Random`.
- **Decision-Support Only**: Academic simulation for demonstrating AI CSP techniques; not intended or certified for clinical use.
