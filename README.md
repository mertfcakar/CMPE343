# Group12 - Local Greengrocer Application

**CMPE 343 - Fall 2025-2026 - Course Project #3**

A JavaFX-based desktop application for managing a local greengrocer business with role-based interfaces for customers, carriers, and owners.

---

## 📋 Project Description

This application provides a complete e-commerce solution for a local greengrocer, featuring three distinct user interfaces based on roles. The system handles product management, order processing, delivery coordination, and business analytics with a MySQL database backend.

---

## 🚀 Getting Started

### 1. First-Time Setup
Run this once to download dependencies and build the project:
- **Windows:** `.\mvnw.cmd clean install`
- **Mac/Linux:** `chmod +x mvnw && ./mvnw clean install`

### 2. Regular Run
Run this command to launch the application:
- **Windows:** `.\mvnw.cmd javafx:run`
- **Mac/Linux:** `./mvnw javafx:run`

---

## 🎯 Features by Role

### 👤 Customer Interface

**Account Management**
- User registration with validation (unique username, strong password)
- Login authentication
- Profile editing (address, contact details)

**Shopping Experience**
- Browse products organized by type (vegetables and fruits)
- View product details with images and prices
- Search and filter products by keyword
- Add items to shopping cart with quantity in kilograms (e.g., 0.5 kg, 2.25 kg)
- Stock availability checking
- Shopping cart on separate window showing items, quantities, prices, and total with VAT

**Order Management**
- Select delivery date and time (within 48 hours of purchase)
- Apply discount coupons from previous purchases
- Automatic loyalty discount based on completed order history
- View order summary before finalizing purchase
- Minimum cart value requirement for checkout
- Order history and delivery status tracking
- Cancel orders within specified time frame
- PDF invoice generation and storage

**Additional Features**
- Rate carriers based on delivery experience (1-5 stars)
- Send messages to owner through application
- View personal delivery schedule

**Dynamic Pricing**
- Regular prices when stock is normal
- Automatic price doubling when product stock ≤ threshold value

### 🚚 Carrier Interface

**Delivery Management**
- View available deliveries with complete order details:
  - Order ID
  - Product list
  - Customer name and address
  - Total amount including VAT
  - Requested delivery date
- Select single or multiple orders to deliver
- Manage current/selected deliveries
- Complete orders by entering actual delivery date

**Order Status Areas**
- **Available**: Orders waiting to be assigned
- **Current/Selected**: Orders accepted by carrier
- **Completed**: Successfully delivered orders

### 👔 Owner Interface

**Product Management**
- Add new products with attributes (name, price, stock, threshold, image)
- Update existing product information
- Remove products from inventory
- Set and adjust threshold values for dynamic pricing
- Upload and manage product images

**Employee Management**
- Hire new carriers
- Remove carriers from system
- View carrier performance ratings

**Order Monitoring**
- View all orders regardless of status
- Track order progression through system
- Monitor delivery performance

**Customer Communication**
- View customer messages
- Reply to customer inquiries

**System Configuration**
- Create and adjust discount coupons
- Set loyalty program standards and requirements
- Configure minimum order values

**Business Analytics**
- Sales reports with visual charts
- Analysis by product performance
- Time-based sales trends
- Revenue tracking and reporting

---

## 🔧 Technical Implementation

### Technologies
- **Frontend**: JavaFX 21 with FXML
- **Backend**: Java 21 (LTS)
- **Build System**: Apache Maven
- **Database**: MySQL 8.0+
- **Libraries**: MySQL Connector/J, iText (PDF generation)

### Design Patterns
- Model-View-Controller (MVC) architecture
- Data Access Object (DAO) pattern
- Object-Oriented Design principles (encapsulation, inheritance, polymorphism)

### Database Features
- **8 Main Tables**: users, products, orders, order_items, coupons, user_coupons, carrier_ratings, messages, loyalty_settings
- **BLOB Storage**: Product images stored in database
- **CLOB Storage**: Transaction logs and PDF invoices
- Foreign key constraints for data integrity
- Indexed fields for optimized queries
- Views for reporting and analytics
- Stored procedures for business logic

### Key Business Rules
- **VAT Rate**: 18% applied to all orders
- **Threshold System**: Prices double when stock ≤ threshold
- **Delivery Window**: Orders must be delivered within 48 hours
- **Minimum Cart Value**: Enforced for checkout
- **Loyalty Discount**: Based on completed order count
- **Cart Merging**: Duplicate products automatically combined

---

## 👥 User Roles

The system supports three user roles with dedicated interfaces:

| Role | Description | Primary Functions |
|------|-------------|-------------------|
| **Customer** | End users purchasing products | Browse, shop, order, track deliveries |
| **Carrier** | Delivery personnel | Accept deliveries, manage routes, complete orders |
| **Owner** | Business administrator | Manage inventory, employees, view analytics |

### Default Accounts

| Username | Password | Role |
|----------|----------|------|
| `cust` | `cust` | Customer |
| `carr` | `carr` | Carrier |
| `own` | `own` | Owner |

---

## 📊 Database Schema

### Core Tables

**users**
- Stores all user accounts with role-based access
- Fields: id, username, password, role, address, contact_details

**products**
- Contains vegetable and fruit inventory
- Fields: id, name, type, price, stock, threshold, image (BLOB)
- Minimum 12 vegetables + 12 fruits

**orders**
- Tracks order lifecycle from creation to delivery
- Fields: id, user_id, carrier_id, order_time, delivery_time, requested_delivery_date, total_cost, status, invoice (CLOB)
- Status: pending → assigned → in_delivery → completed

**order_items**
- Individual products within each order
- Fields: id, order_id, product_id, product_name, quantity, unit_price, total_price

**carrier_ratings**
- Customer feedback on carrier performance
- Fields: id, order_id, carrier_id, customer_id, rating (1-5), comment

**messages**
- Customer-owner communication channel
- Fields: id, sender_id, receiver_id, subject, message, is_read

**coupons**
- Discount coupon definitions
- Fields: id, code, discount_percentage, min_purchase_amount, valid_until

**loyalty_settings**
- Loyalty program configuration
- Fields: id, min_orders, discount_percentage, description

---

## 🎨 User Interface Specifications

### Window Properties
- **Initial Size**: 960 × 540 pixels
- **Position**: Centered on screen
- **Resizable**: Yes (elements scale proportionally)
- **Title Format**: "GroupXX GreenGrocer"

### Layout Requirements
- Username displayed in corner (customer interface)
- Products organized under TitledPanes by category
- Product listings sorted alphabetically by name
- Shopping cart on separate stage/window
- Product display includes: name, image, price

### Design Considerations
- Minimum 6 different event handlers
- FXML files created with SceneBuilder
- Separate controller classes for each view
- Backend and frontend code separation
- Responsive design for window resizing

---

## 📦 Project Structure

```
Group12-GreenGrocer/
├── src/main/java/com/group12/greengrocer/
│   ├── Main.java
│   ├── controllers/        # FXML controllers
│   ├── models/            # Data models (User, Product, Order)
│   ├── database/          # Database adapter and DAO classes
│   └── utils/             # Helper classes
├── src/main/resources/
│   ├── fxml/             # JavaFX interface files
│   ├── images/           # Product images
│   └── css/              # Stylesheets
├── database/
│   └── Group12.sql       # Database schema and sample data
└── pom.xml              # Maven configuration
```

---

## 👥 Team Members

- Mert Fahri Çakar
- Nermin Sipahioğlu
- Burak Arslan
- Hüseyin Yiğit Şahin

---

## 📄 Course Information

- **Course**: CMPE 343 - Object Oriented Programming
- **Instructor**: Asst. Prof. Dr. İlktan Ar
- **Institution**: Kadir Has University
- **Semester**: Fall 2025-2026

---

## 📝 License

This project is developed for educational purposes as part of CMPE 343 course requirements.