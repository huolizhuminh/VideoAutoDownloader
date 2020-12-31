package com.minhui.vpn.udpip;
/**
 * Created by minhui.zhu on 2017/6/24.
 * Copyright © 2017年 minhui.zhu. All rights reserved.
 */

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

/**
 * Representation of an IP Packet
 */

public class Packet implements Serializable {
    public static final int IP4_HEADER_SIZE = 20;
    public static final int TCP_HEADER_SIZE = 20;
    public static final int UDP_HEADER_SIZE = 8;
    private static final int FIRST_TCP_DATA = 40;
    private static final String TAG = "Packet";


    public IP4Header ip4Header;
    public UDPHeader udpHeader;
    public ByteBuffer backingBuffer;


    public int playLoadSize = 0;


    public Packet(ByteBuffer buffer) throws UnknownHostException {
        this.ip4Header = new IP4Header(buffer);
        this.udpHeader = new UDPHeader(buffer);
        this.backingBuffer = buffer;
        this.playLoadSize = buffer.limit() - buffer.position();
    }

    private Packet() {

    }

    public Packet duplicated() {
        Packet packet = new Packet();
        packet.ip4Header = ip4Header.duplicate();

        if (udpHeader != null) {
            packet.udpHeader = udpHeader.duplicate();
        }
        return packet;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Packet{");
        sb.append("ip4Header=").append(ip4Header);
        sb.append(", udpHeader=").append(udpHeader);
        sb.append(", payloadSize=").append(backingBuffer.limit() - backingBuffer.position());
        sb.append('}');
        return sb.toString();
    }


    public void swapSourceAndDestination() {
        InetAddress newSourceAddress = ip4Header.destinationAddress;
        ip4Header.destinationAddress = ip4Header.sourceAddress;
        ip4Header.sourceAddress = newSourceAddress;
        short newSourcePort = udpHeader.destinationPort;
        udpHeader.destinationPort = udpHeader.sourcePort;
        udpHeader.sourcePort = newSourcePort;
    }



    public void updateUDPBuffer(ByteBuffer buffer, int payloadSize) {
        buffer.position(0);
        fillHeader(buffer);
        backingBuffer = buffer;

        int udpTotalLength = UDP_HEADER_SIZE + payloadSize;
        backingBuffer.putShort(IP4_HEADER_SIZE + 4, (short) udpTotalLength);
        udpHeader.length = udpTotalLength;

        // Disable UDP checksum validation
        backingBuffer.putShort(IP4_HEADER_SIZE + 6, (short) 0);
        udpHeader.checksum = 0;

        int ip4TotalLength = IP4_HEADER_SIZE + udpTotalLength;
        backingBuffer.putShort(2, (short) ip4TotalLength);
        ip4Header.totalLength = ip4TotalLength;

        updateIP4Checksum();
        this.playLoadSize = payloadSize;
    }

    private void updateIP4Checksum() {
        ByteBuffer buffer = backingBuffer.duplicate();
        buffer.position(0);

        // Clear previous checksum
        buffer.putShort(10, (short) 0);

        int ipLength = ip4Header.headerLength;
        int sum = 0;
        while (ipLength > 0) {
            sum += BitUtils.getUnsignedShort(buffer.getShort());
            ipLength -= 2;
        }
        while (sum >> 16 > 0) {
            sum = (sum & 0xFFFF) + (sum >> 16);
        }
        sum = ~sum;
        ip4Header.headerChecksum = sum;
        backingBuffer.putShort(10, (short) sum);
    }



    private void fillHeader(ByteBuffer buffer) {
        ip4Header.fillHeader(buffer);
        udpHeader.fillHeader(buffer);

    }

    public String getIpAndPort() {
        String ipAndrPort;
        if (ip4Header == null) {
            return null;
        }
        int destinationPort;
        int sourcePort;
        InetAddress destinationAddress = ip4Header.destinationAddress;
        destinationPort = udpHeader.destinationPort;
        sourcePort = udpHeader.sourcePort;
        ipAndrPort = "UDP:" + destinationAddress.getHostAddress() + ":" + destinationPort + " " + sourcePort;
        return ipAndrPort;
    }





    /**
     * IP头部总共20个字节
     * char m_cVersionAndHeaderLen;     　　//版本信息(前4位)，头长度(后4位)  1个
     * char m_cTypeOfService;      　　　　　 // 服务类型8位    1个
     * short m_sTotalLenOfPacket;    　　　　//数据包长度      2个
     * short m_sPacketID;      　　　　　　　 //数据包标识      2个
     * short m_sSliceinfo;      　　　　　　　  //分片使用     2个
     * char m_cTTL;        　　　　　　　　　　//存活时间       1个
     * char m_cTypeOfProtocol;    　　　　　 //协议类型       1个
     * short m_sCheckSum;      　　　　　　 //校验和          2个
     * unsigned int m_uiSourIp;     　　　　　//源ip         4个
     * unsigned int m_uiDestIp;     　　　　　//目的ip        4个
     */
    public static class IP4Header implements Serializable {

        public byte version;
        byte IHL;
        int headerLength;
        short typeOfService;
        int totalLength;

        int identificationAndFlagsAndFragmentOffset;

        short TTL;
        private short protocolNum;
        TransportProtocol protocol;
        int headerChecksum;

        public InetAddress sourceAddress;
        public InetAddress destinationAddress;

        int optionsAndPadding;

        IP4Header duplicate() {
            IP4Header ip4Header = new IP4Header();
            ip4Header.version = version;
            ip4Header.IHL = IHL;
            ip4Header.headerLength = headerLength;
            ip4Header.typeOfService = typeOfService;
            ip4Header.totalLength = totalLength;

            ip4Header.identificationAndFlagsAndFragmentOffset = identificationAndFlagsAndFragmentOffset;

            ip4Header.TTL = TTL;
            ip4Header.protocolNum = protocolNum;
            ip4Header.protocol = protocol;
            ip4Header.headerChecksum = headerChecksum;

            ip4Header.sourceAddress = sourceAddress;
            ip4Header.destinationAddress = destinationAddress;

            ip4Header.optionsAndPadding = optionsAndPadding;
            return ip4Header;
        }

        private IP4Header() {

        }

        private enum TransportProtocol {
            TCP(6),
            UDP(17),
            Other(0xFF);

            private int protocolNumber;

            TransportProtocol(int protocolNumber) {
                this.protocolNumber = protocolNumber;
            }

            private static TransportProtocol numberToEnum(int protocolNumber) {
                if (protocolNumber == 6) {
                    return TCP;
                } else if (protocolNumber == 17) {
                    return UDP;
                } else {
                    return Other;
                }

            }

            public int getNumber() {
                return this.protocolNumber;
            }
        }

        private IP4Header(ByteBuffer buffer) throws UnknownHostException {
            byte versionAndIHL = buffer.get();
            this.version = (byte) (versionAndIHL >> 4);
            this.IHL = (byte) (versionAndIHL & 0x0F);
            this.headerLength = this.IHL << 2;

            this.typeOfService = BitUtils.getUnsignedByte(buffer.get());
            this.totalLength = BitUtils.getUnsignedShort(buffer.getShort());

            this.identificationAndFlagsAndFragmentOffset = buffer.getInt();

            this.TTL = BitUtils.getUnsignedByte(buffer.get());
            this.protocolNum = BitUtils.getUnsignedByte(buffer.get());
            this.protocol = TransportProtocol.numberToEnum(protocolNum);
            this.headerChecksum = BitUtils.getUnsignedShort(buffer.getShort());

            byte[] addressBytes = new byte[4];
            buffer.get(addressBytes, 0, 4);
            this.sourceAddress = InetAddress.getByAddress(addressBytes);

            buffer.get(addressBytes, 0, 4);
            this.destinationAddress = InetAddress.getByAddress(addressBytes);

            //this.optionsAndPadding = buffer.getInt();
        }

        void fillHeader(ByteBuffer buffer) {
            buffer.put((byte) (this.version << 4 | this.IHL));
            buffer.put((byte) this.typeOfService);
            buffer.putShort((short) this.totalLength);

            buffer.putInt(this.identificationAndFlagsAndFragmentOffset);

            buffer.put((byte) this.TTL);
            buffer.put((byte) this.protocol.getNumber());
            buffer.putShort((short) this.headerChecksum);

            buffer.put(this.sourceAddress.getAddress());
            buffer.put(this.destinationAddress.getAddress());
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("IP4Header{");
            sb.append("version=").append(version);
            sb.append(", IHL=").append(IHL);
            sb.append(", typeOfService=").append(typeOfService);
            sb.append(", totalLength=").append(totalLength);
            sb.append(", identificationAndFlagsAndFragmentOffset=").append(identificationAndFlagsAndFragmentOffset);
            sb.append(", TTL=").append(TTL);
            sb.append(", protocol=").append(protocolNum).append(":").append(protocol);
            sb.append(", headerChecksum=").append(headerChecksum);
            sb.append(", sourceAddress=").append(sourceAddress.getHostAddress());
            sb.append(", destinationAddress=").append(destinationAddress.getHostAddress());
            sb.append('}');
            return sb.toString();
        }
    }



    public static class UDPHeader implements Serializable {
        public short sourcePort;
        public short destinationPort;

        public int length;
        public int checksum;

        UDPHeader(ByteBuffer buffer) {
            this.sourcePort = buffer.getShort();
            this.destinationPort =buffer.getShort();

            this.length = BitUtils.getUnsignedShort(buffer.getShort());
            this.checksum = BitUtils.getUnsignedShort(buffer.getShort());
        }

        UDPHeader() {

        }

        void fillHeader(ByteBuffer buffer) {
            buffer.putShort( this.sourcePort);
            buffer.putShort(this.destinationPort);

            buffer.putShort((short) this.length);
            buffer.putShort((short) this.checksum);
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("UDPHeader{");
            sb.append("sourcePort=").append(BitUtils.getUnsignedShort(sourcePort));
            sb.append(", destinationPort=").append(BitUtils.getUnsignedShort(destinationPort));
            sb.append(", playoffSize=").append(length);
            sb.append(", checksum=").append(checksum);
            sb.append('}');
            return sb.toString();
        }

        UDPHeader duplicate() {
            UDPHeader udpHeader = new UDPHeader();
            udpHeader.sourcePort = sourcePort;
            udpHeader.destinationPort = destinationPort;
            udpHeader.length = length;
            udpHeader.checksum = checksum;
            return udpHeader;
        }
    }

    private static class BitUtils {
        private static short getUnsignedByte(byte value) {
            return (short) (value & 0xFF);
        }

        private static int getUnsignedShort(short value) {
            return value & 0xFFFF;
        }

        private static long getUnsignedInt(int value) {
            return value & 0xFFFFFFFFL;
        }
    }
}
