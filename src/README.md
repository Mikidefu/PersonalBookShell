# Pre-Configuration

✅ 1. Download ngrok (if you haven’t yet)

Go to https://ngrok.com/download, download the Windows ZIP file, and extract it somewhere (e.g., C:\ngrok).

✅ 2. Authenticate ngrok (first time only)
You need to add your auth token:

powershell

    .\ngrok.exe config add-authtoken YOUR_AUTH_TOKEN
You can find your auth token here: https://dashboard.ngrok.com/get-started/setup

✅ 3. Start a tunnel

Run a tunnel to a local port 8080:

powershell

    .\ngrok.exe http 8080
✅ 4. Access the public URL
After starting the tunnel, ngrok will show something like:


Forwarding    https://abc123.ngrok.io -> http://localhost:8080

You have to put that public URL into the main.dart of the flutter app.

---------------------

# 📚 BookCollectionApp

A desktop and mobile application to manage and track your book collection, with support for automatic ISBN scanning via a mobile device camera.

## ⚙️ Requirements

- **Java 17+**
- **PostgreSQL** (database must be configured beforehand)
- **Flutter 3.x**
- **Ngrok**
- **Compatible IDEs:**
    - IntelliJ IDEA (for Java)
    - Visual Studio Code (for Flutter)
    - Other compatible IDEs are also supported

---

## ▶️ Setup Instructions

### 1. Start the Spark Backend

Open your IDE (e.g., IntelliJ IDEA) and run the following file:

    src/spark/BookServer.java


This will start the Spark backend server, which handles communication between the Java app and the mobile application.

---

### 2. Launch the Java Desktop Application

In the same IDE (or another compatible one), run:

    src/app/BookCollectionApp.java


This is the main JavaFX application for managing your local book collection.

---

### 3. Start Ngrok Tunnel

Open **PowerShell** or any terminal and run:

```bash
ngrok http 8080
```

Ngrok will provide you with a public URL, like:

    https://random-id.ngrok.io

### 4. Configure the Flutter Mobile App

link to the appMobile repo: https://github.com/Mikidefu/isbn_reader/tree/main/isbn_reader/lib

   Open the file:

    lib/main.dart
and replace the value of the BackendUrl variable with the Ngrok URL, for example:


    const String BackendUrl = "https://random-id.ngrok.io/books";
Save the file.

### 5. Run the Flutter Mobile App
   Open Visual Studio Code (or any Flutter-supported IDE) and run:


  ```bash 
  flutter run
  ```
Make sure a device is connected (either an emulator or a physical device), and that Flutter is properly set up.

✅ At This Point, Everything Should Be Functional!
The desktop app allows manual entry and statistics tracking of books.

The mobile app allows ISBN scanning and one-click insertion into the desktop system via backend communication.
