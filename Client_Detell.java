import java.io.*; // ใช้สำหรับการทำงานกับอินพุตและเอาต์พุต เช่น การอ่านและเขียนไฟล์
import java.net.*; // ใช้สำหรับการทำงานกับเครือข่าย เช่น การเชื่อมต่อกับเซิร์ฟเวอร์

public class Client_Detell {
    private static final String SERVER_ADDRESS = "172.18.115.89"; // ที่อยู่ IP ของเซิร์ฟเวอร์ที่จะเชื่อมต่อ
    private static final int SERVER_PORT = 8080; // หมายเลขพอร์ตที่เซิร์ฟเวอร์รับฟังการเชื่อมต่อ

    public static void main(String[] args) {
        // ใช้ try-with-resources เพื่อให้แน่ใจว่าทรัพยากรถูกปิดโดยอัตโนมัติ
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT); // สร้างซ็อกเก็ตเพื่อเชื่อมต่อกับเซิร์ฟเวอร์ที่อยู่ใน SERVER_ADDRESS และพอร์ต SERVER_PORT
             DataInputStream in = new DataInputStream(socket.getInputStream()); // อ่านข้อมูลจากเซิร์ฟเวอร์
             DataOutputStream out = new DataOutputStream(socket.getOutputStream())) { // ส่งข้อมูลไปยังเซิร์ฟเวอร์

            // รับรายการไฟล์ที่มีอยู่จากเซิร์ฟเวอร์
            String fileList = in.readUTF(); // อ่านรายการไฟล์ที่มีอยู่จากเซิร์ฟเวอร์ซึ่งถูกส่งมาในรูปแบบสตริง (UTF-8)
            System.out.println("Available files:\n" + fileList);  // แสดงรายการไฟล์ให้ผู้ใช้ดู

            // ขอไฟล์จากเซิร์ฟเวอร์
            System.out.println("Name of the file you want to download...");
            System.out.print("Enter Here ===> ");
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in)); // สร้าง BufferedReader สำหรับการอ่านข้อมูลที่ผู้ใช้ป้อน
            String requestedFile = reader.readLine(); // อ่านชื่อไฟล์ที่ผู้ใช้ป้อนและเก็บไว้ในตัวแปร requestedFile
            out.writeUTF(requestedFile); // ส่งชื่อไฟล์ที่ร้องขอไปยังเซิร์ฟเวอร์

            // รับและบันทึกไฟล์
            long fileSize = in.readLong(); // อ่านขนาดไฟล์ที่เซิร์ฟเวอร์ส่งมา
            if (fileSize > 0) { // ตตรวจสอบว่าไฟล์มีขนาดมากกว่า 0 (หมายถึงไฟล์มีอยู่จริง)
                try (FileOutputStream fos = new FileOutputStream("./client_files/" + requestedFile)) { // เปิด FileOutputStream เพื่อบันทึกไฟล์ลงในไดเรกทอรี client_files ภายใต้ชื่อที่ร้องขอ
                    byte[] buffer = new byte[4096]; // สร้างบัฟเฟอร์สำหรับเก็บข้อมูลไฟล์และอ่านข้อมูลไฟล์ทีละส่วน ขนาด 4096 ไบต์
                    long remaining = fileSize; // ตั้งค่าตัวแปร remaining เป็นขนาดของไฟล์ที่เหลือในการอ่าน
                    int read; // สำหรับเก็บจำนวนไบต์ที่อ่านได้ในแต่ละครั้ง
                    while ((read = in.read(buffer, 0, (int) Math.min(buffer.length, remaining))) > 0) { // อ่านข้อมูลทีละส่วนจนกว่าไฟล์ทั้งหมดจะถูกอ่าน
                        fos.write(buffer, 0, read); // เขียนข้อมูลที่อ่านได้ลงในไฟล์
                        remaining -= read; // ลดจำนวนไบต์ที่เหลือตามจำนวนที่อ่านไปแล้ว
                    }
                }
                System.out.println("File downloaded successfully!"); // ยืนยันการดาวน์โหลดไฟล์
            } else {
                System.out.println("File not found on server."); // แจ้งผู้ใช้หากไฟล์ไม่พบในเซิร์ฟเวอร์
            }

        } catch (IOException e) { // จัดการข้อผิดพลาดที่เกี่ยวข้องกับการทำงานของ I/O
            e.printStackTrace(); // แสดงรายละเอียดข้อผิดพลาดสำหรับการดีบัก
        }
    }
}
