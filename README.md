## How to Install the Library

1. Clone the repository:
   ```bash
   git clone https://github.com/groundbreakingmc/MyLib.git
   ```

2. Navigate to the project directory:
   ```
   cd your-repo
   ```

3. Run the installation script:
   - **On Windows**:
     ```
     install-mylib.bat 1.1
     ```
   - **On Linux/macOS**:
     ```
     ./install-mylib.sh 1.1
     ```

After this, the library will be available in your local Maven repository. You can add it to your project using:
```xml
<dependency>
    <groupId>com.github.groundbreakingmc</groupId>
    <artifactId>MyLib</artifactId>
    <version>1.0</version>
</dependency>```