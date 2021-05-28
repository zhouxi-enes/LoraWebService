package org.xi.lora.webservice;

import com.google.gson.Gson;
import org.xi.lora.webservice.other.MyServlet;
import org.xi.lora.webservice.pojo.MessageObject;
import org.xi.lora.webservice.pojo.ResultObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;

public class SafeReportServlet extends MyServlet {

    private static final int LORA_PORT = 35000;

    private static final String LORA_SERVER_ADDRESS = "127.0.0.1";

    private static final int waiting_time = 120 * 1000;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
        String name = req.getParameter("n");
        String id = req.getParameter("i");
        String comment = req.getParameter("c");
        MessageObject messageObject = new MessageObject();
        messageObject.c = comment;
        messageObject.i = id;
        messageObject.n = name;
        Gson gson = new Gson();
        String json = gson.toJson(messageObject);
        System.out.println(json);
        InetAddress inetAddress = Inet4Address.getByName(LORA_SERVER_ADDRESS);
        DatagramSocket datagramSocket = new DatagramSocket();
        //先發送一個封包
        byte[] bytes = json.getBytes();
        DatagramPacket datagramPacket = new DatagramPacket(bytes,0,bytes.length,inetAddress,LORA_PORT);
        datagramSocket.setSoTimeout(waiting_time);
        datagramSocket.send(datagramPacket);
        //然後就監聽
        boolean isDataReceived = false;
        byte[] buffer = new byte[1024];
        DatagramPacket datagramPacketReceive = new DatagramPacket(buffer,0,buffer.length);
        try{
            datagramSocket.receive(datagramPacketReceive);
            isDataReceived = true;
        }catch (Exception e) {
            e.printStackTrace();
        }
        String return_str;
        if(isDataReceived) {
            //開始讀udp
            int length = datagramPacketReceive.getLength();
            int offset = datagramPacketReceive.getOffset();
            return_str = new String(datagramPacketReceive.getData(),offset,length);
        } else {
            ResultObject resultObject = new ResultObject();
            resultObject.result = ResultObject.RESULT_FAILED;
            return_str = gson.toJson(resultObject);
        }
        resp.getWriter().print(return_str);
    }
}
