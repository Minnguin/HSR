import java.io.*; //Input Output เช่น การอ่านและเขียนไฟล์
import java.net.*; //สร้างเซิร์ฟเวอร์เและชื่อมต่อกับไคลเอนต์
import java.util.HashMap; //เก็บข้อมูลเกี่ยวกับการเชื่อมต่อของ Client แต่ละราย
import java.util.Map; //Interface สำหรับการจัดเก็บข้อมูลแบบคีย์-ค่า

//เมื่อมีไคลเอ็นต์เชื่อมต่อเข้ามา Server จะสร้าง Thread ใหม่ขึ้นมา 1 ตัวเพื่อให้บริการแก่ Client รายนั้นๆ โดย Thread นี้จะถูกสร้างขึ้นจากคลาส ClientHandler ซึ่ง Implement interface Runnable

public class Server_Detail {

    //กำหนดค่า Server
    private static final int PORT = 8080;
    private static final String DIRECTORY = "./server_files/";
    private static final Map<Socket, Integer> clientThreadCountMap = new HashMap<>(); //clientThreadCountMap จำนวน Thread ที่กำลังให้บริการกับ Client นั้นๆ

    //เริ่มต้น Server
    public static void main(String[] args) {

        //สร้าง ServerSocket ไว้ฟังคำขอของ PORT ที่กำหนด
        /*ใช้ Try Catch ดักจับ error เพื่อป้องกันไม่ให้โปรแกรมหยุดทำงานเมื่อเจอข้อผิดพลาด เช่น
            - ไม่เจอไฟล์ที่ Client ขอ FileNotFoundException
            - ปัญหาการเชื่อมต่อเครือข่าย IOException
        */
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on port: " + PORT);

            while (true) { //วนลูปอย่างไม่สิ้นสุดเพื้่อสร้าง Thread และรับคำขอจาก Client (ลูปนี้เป็นส่วนที่เกิด Multi-threading)
                Thread t1 = new Thread(new ClientHandler(serverSocket.accept())); 
                /*ClientHandler
                    จะรับผิดชอบในการจัดการการเชื่อมต่อกับไคลเอ็นต์แต่ละราย ซึ่งรวมถึงการรับคำขอไฟล์ การส่งไฟล์ และการปิดการเชื่อมต่อ
                */
                t1.start(); //เริ่มการทำงาของ Thread โดยทำให้ Thread เปลี่ยนสถานะเป็นพร้อมที่จะทำงาน
            }
        } catch (IOException e) {
            e.printStackTrace(); //แสดงรายละเอียดและตำแหน่งข้อผิดพลาดออกมา
        }
    }

    private static class ClientHandler implements Runnable { 
        /*
            Runnable ทำให้แต่ละการเชื่อมต่อของ Client สามารถทำงานได้แบบขนานกัน โดยไม่รบกวนการทำงานของการเชื่อมต่ออื่นๆ
            รองรับการเชื่อมต่อจากไคลเอ็นต์ได้หลายรายพร้อมกัน
        */
        private Socket socket; //ทำให้ Server และ Client ส่งและรับข้อมูลซึ่งกันและกันได้ สามารถสื่อสารกันได้ผ่านเครือข่าย

        public ClientHandler(Socket socket) { //Constructor
            this.socket = socket;
        }

        @Override
        public void run() { //เริ่มต้นการทำงานจาก method start()
            synchronized (clientThreadCountMap) { //clientThreadCountMap เก็บข้อมูลเกี่ยวกับจำนวน Thread ที่ให้บริการกับ Client แต่ละราย
                /* Synchronized
                    - การใช้ synchronized มีความสำคัญในการรักษาความถูกต้องของข้อมูลและป้องกันปัญหาที่อาจเกิดขึ้นจากการทำงานแบบ Multi-threading โดยเฉพาะอย่างยิ่งในส่วนที่เกี่ยวข้องกับการจัดการ clientThreadCountMap
                    - การใช้ synchronized อย่างไม่ถูกต้องอาจทำให้เกิด Deadlock (Thread หลาย Thread รอให้กันและกันปล่อDย Lock ทำให้โปรแกรมหยุดทำงาน)
                */

                //containsKey(socket) ถ้า Client ยังไม่เคยเชื่อมต่อกับ Server คืนค่าเป็น Fasle
                if (!clientThreadCountMap.containsKey(socket)) { //ตรวจสอบว่า clientThreadCountMap มี Socket นี้เป็นคีย์หรือไม่ (หมายความว่าเช็คว่าไคลเอนต์นี้เคยเชื่อมต่อมาก่อนหรือไม่)
                    clientThreadCountMap.put(socket, 1); 
                    /*หากเงื่อนไขใน if เป็นจริง (คือไคลเอนต์นี้ยังไม่เคยเชื่อมต่อ) จะทำการเพิ่มรายการใหม่ใน clientThreadCountMap
                    โดยใช้ socket เป็นคีย์ และกำหนดค่าเริ่มต้นเป็น 1 ซึ่งแสดงว่า *** ไคลเอนต์นี้มีการเชื่อมต่ออยู่ 1 เธรด *** */
                } else {
                    int currentCount = clientThreadCountMap.get(socket); //ดึงค่าปัจจุบันของจำนวน Thread ที่เชื่อมต่อกับ Client มาใส่ในตัวแปร currentCount
                    clientThreadCountMap.put(socket, currentCount + 1);
                    //อัปเดตค่าของ clientThreadCountMap โดยเพิ่มจำนวนจากเดิม 1 เธรด (บวก 1) และนำค่าที่อัปเดตแล้วใส่กลับเข้าไปใน clientThreadCountMap
                }
            }
            //DataInputStream และ DataOutputStream เป็นคลาสใน Java ที่ใช้สำหรับอ่านและส่งข้อมูล ระหว่าง Server และ Client (แบบไบต์ Byte)
            //ส้รางตัว input output ที่ทำงานระหว่างตัว Server และ Client โดยผ่านเจ้าตัว socket
            try (DataInputStream in = new DataInputStream(socket.getInputStream()); //รับข้อมูลที่ Client ส่งมายัง Server ผ่าน Socket
                 DataOutputStream out = new DataOutputStream(socket.getOutputStream())) { //่ส่งข้อมูลจาก Server ไปยัง Client ผ่าน Socket
                synchronized (clientThreadCountMap) {
                    System.out.println("Client: " + socket.getPort()); //หมายเลขพอร์ตที่ Client ใช้ในการเชื่อมต่อ
                    int threadCountForClient = clientThreadCountMap.get(socket); //เก็บค่าที่เป็นจำนวนเธรดที่เชื่อมต่อกับไคลเอนต์ที่อ้างอิงโดย socket นั้น
                    System.out.println("Threads connected to this client: " + threadCountForClient); //แสดงจำนวน Thread ที่กำลังให้บริการกับ Client นั้นออกมา
                }

                File[] files = new File(DIRECTORY).listFiles(); //บรรทัดนี้จะทำการสำรวจไฟล์ทั้งหมดภายในไดเร็กทอรีที่กำหนดไว้ และเก็บรายชื่อไฟล์เหล่านั้นไว้ในตัวแปร files
                //สร้างอ็อบเจ็กต์ File ที่ชี้ไปยังไดเรกทอรีที่ระบุด้วยตัวแปร DIRECTORY และเรียกใช้เมธอด listFiles() ซึ่งจะคืนค่ามาเป็นอาเรย์ของอ็อบเจ็กต์ File ที่แทนไฟล์และไดเรกทอรีทั้งหมดภายในไดเรกทอรีนั้น
                
                StringBuilder fileList = new StringBuilder(); //สร้างอ็อบเจ็กต์ StringBuilder ที่ใช้สำหรับการสร้างสตริงที่มีการปรับปรุงบ่อยๆ โดยที่ไม่ต้องสร้างอ็อบเจ็กต์สตริงใหม่ทุกครั้งที่มีการปรับปรุง
                for (File file : files) { //วนลูปผ่านไฟล์ทั้งหมดในอาเรย์ files
                    fileList.append("- ").append(file.getName()).append("\n"); //ในแต่ละรอบของการวนลูป จะนำชื่อของไฟล์นั้นมาต่อกันที่ท้ายของ StringBuilder ที่ชื่อ fileList
                }
                out.writeUTF(fileList.toString());
                /*
                    - แปลง StringBuilder เป็นสตริงปกติด้วย toString() และเขียนสตริงนั้นไปยัง DataOutputStream (หรืออ็อบเจ็กต์ out อื่นๆ) โดยใช้เมธอด writeUTF()
                    - เมธอด writeUTF() จะเขียนข้อมูลในรูปแบบ UTF-8 ซึ่งเป็นรูปแบบการเข้ารหัสที่นิยมใช้กัน
                */

                String requestedFile = in.readUTF(); //ชื่อไฟล์ที่ไคลเอ็นต์ร้องขอ ซึ่งได้มาจากการอ่านค่าจาก InputStream
                //readUTF() อ่านสตริงที่ถูกเขียนโดยใช้ writeUTF() และคืนค่ากลับมาเป็นสตริงปกติ

                //fileToSend จะเป็นอ็อบเจ็กต์ File ที่แทนไฟล์ที่ Client ร้องขอโดยมีเส้นทางรวมของ DIRECTORY และ requestedFile
                File fileToSend = new File(DIRECTORY + requestedFile); 
                //สร้างวัตถุ File ที่จะแทนถึงไฟล์ที่จะส่งให้ Client เพื่อเตรียมพร้อมสำหรับการส่งไฟล์ไปให้ไคลเอ็นต์ โดยการสร้าง path ที่ถูกต้องของไฟล์นั้น(+ คือเชื่อมต่อเข้ากันพื่อสร้าง path ของไฟล์ที่ต้องการ)

                //ไฟล์ที่ต้องการส่งมีเก็บอยู่ในตัวแปร fileToSend หรือไม่
                if (fileToSend.exists()) { //ใช้ method exists() ของวัตถุ File ตรวจสอบว่าไฟล์ที่แทนด้วยวัตถุนี้มีอยู่จริงใน Server หรือไม่
                    
                    //ถ้าไฟล์มีอยู่ส่งขนาดของไฟล์ไปยังอ็อบเจ็กต์ DataOutputStream
                    out.writeLong(fileToSend.length()); //ส่งขนาดของไฟล์ที่ได้ไปยัง Client เป็นขั้นตอนสำคัญในการส่งไฟล์ เนื่องจากการแจ้งขนาดไฟล่วงหน้าจะช่วยให้กระบวนการรับส่งข้อมูลเป็นไปอย่างมีประสิทธิภาพและถูกต้อง
                    
                    //เปิดอ่านไฟล์และส่งข้อมูลไฟล์ไปยังไคลเอ็นต์ทีละส่วน
                    try (FileInputStream fis = new FileInputStream(fileToSend)) { //ใช้ FileInputStream เพื่อเปิดอ่านข้อมูลจากไฟล์ที่แทนด้วย fileToSend
                        byte[] buffer = new byte[4096]; //สร้างบัฟเฟอร์ที่เป็นอาร์เรย์ของไบต์ขนาด 4096 ไบต์ (4 KB) ใช้เพื่ออ่านข้อมูลจากไฟล์เป็นบล็อกแทนการอ่านทีละไบต์
                        /*
                            ขนาดของ Buffer มีผลต่อประสิทธิภาพในการอ่านและเขียนข้อมูลจากไฟล์
                            * โดยทั่วไปแล้ว ขนาดของ Buffer ที่นิยมใช้คือ 4096 bytes (4 KB) หรือ 8192 bytes (8 KB) ซึ่งเป็นขนาดที่เหมาะสมสำหรับการทำงานทั่วไป
                        */
                        int read; //เก็บจำนวนไบต์ที่อ่านได้จากไฟล์ในแต่ละครั้ง

                        //เป็นลูป while ที่ทำการอ่านข้อมูลจากไฟล์ซ้ำๆ จนกว่าจะอ่านข้อมูลจนหมด
                        while ((read = fis.read(buffer)) != -1) { //อ่านข้อมูลจากไฟล์และเก็บไว้ใน buffer
                            /*
                                fis.read(buffer) จะคืนค่าจำนวนไบต์ที่อ่านได้หรือ -1 ถ้าหมดข้อมูล
                                ทำซ้ำจนกว่า fis.read() จะคืนค่า -1 ซึ่งหมายความว่าไม่มีข้อมูลเหลือให้อ่าน
                            */
                            out.write(buffer, 0, read); //ส่งข้อมูลที่อ่านได้จาก buffer ไปยังไคลเอ็นต์ด้วย write() ของ DataOutputStream
                            /*
                                - buffer คือ buffer ที่เก็บข้อมูล
                                0 คือ offset (ตำแหน่งเริ่มต้น) ของข้อมูลใน buffer
                                read คือ จำนวน bytes ที่จะส่ง (เก็บจำนวนไบต์ที่อ่านได้จากไฟล์ในแต่ละครั้ง)
                            */
                        }
                    }
                } else { //file ที่ต้องการส่งไม่พบใน Server

                    // out ยังคงเป็น DataOutputStream ที่ใช้สำหรับส่งข้อมูลไปยัง Client
                    out.writeLong(0); //เขียนหรือส่งค่า 0 ไปยังสตรีม out ความหมายคือ การแจ้งให้ Client รู้ว่าไม่เจอไฟล์ที่ร้องขอ หรือมีขนาดเป็น 0 bytes (ไฟล์ว่าง)
                    //เขียนค่า 0 ไปยังสตรีม ซึ่งหมายความว่าไม่มีไฟล์หรือขนาดของไฟล์คือ 0 (ซึ่งแสดงว่าไม่มีข้อมูลสำหรับส่ง)
                }
            } catch (IOException e) {
                e.printStackTrace();
            
            //ทำหลังจาก Try-Catch
            //รับผิดชอบในการจัดการการปิดการเชื่อมต่อของ Client โดยจะลดจำนวน Thread ที่ให้บริการกับ Client ลง หรือลบข้อมูล Client ออกจาก clientThreadCountMap เมื่อไม่มี Thread ให้บริการแล้ว โดยไม่คำนึงถึงว่าเกิดข้อผิดพลาดในส่วน try หรือไม่
            } finally { //finally จะทำงานเสมอเมื่อบล็อก try และ catch เสร็จสิ้น โดยไม่สนใจว่าจะเกิดข้อผิดพลาดหรือไม่ ณ ที่นี้หมายถึงการปิดการเชื่อมต่อของ Client 
                synchronized (clientThreadCountMap) { 
                    //synchronized เพื่อเข้าถึง clientThreadCountMap ได้อย่างปลอดภัยในกรณีที่มีการทำงานแบบ Multi-Threading
                    
                    int currentCount = clientThreadCountMap.get(socket); //ดึงจำนวน Thread ปัจจุบันที่ให้บริการกับ Client นี้ใส่ในตัวแปร currentCount
                    if (currentCount > 1) { //ตรวจสอบว่าจำนวนเธรดที่เชื่อมต่อกับ socket มีมากกว่าหนึ่งหรือไม่
                        clientThreadCountMap.put(socket, currentCount - 1); //ถ้าใช่ ลดจำนวน Thread ลง 1 ตัว
                    } else { //ถ้ามีเพียง 1 ตัวหรือไม่มีเลย:
                        clientThreadCountMap.remove(socket); //ลบ socket ออกจาก clientThreadCountMap เพราะไม่มีเธรดที่เชื่อมต่ออยู่แล้ว
                        System.out.println("Client disconnected: " + socket); //แสดงข้อความ "Client disconnected: " พร้อมกับข้อมูล Socket บนคอนโซล
                    }
                }
            }
        }
    }
}
