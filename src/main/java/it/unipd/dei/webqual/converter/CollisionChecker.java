package it.unipd.dei.webqual.converter;

import static it.unipd.dei.webqual.converter.Utils.isHead;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;

public class CollisionChecker {
	private static final String links_file_location=null;//TODO write location
	private static final int numB=8;
	private static final int numURL=368400000;
	private static byte[][] urls=new byte[numURL][numB];
	public static void main(String[] args) throws IOException{
		DataInputStream dis=new DataInputStream(new BufferedInputStream(new FileInputStream(links_file_location)));
		byte[] buff=new byte[numB];
		int counter=0;
		while(true){
			int read=dis.read(buff);
			if(read==numB){//enough byte
				if(isHead(buff)){
					checkPresence(buff, counter);
					urls[counter++]=buff;
				}
			} else {
				break;
			}
		}
		System.out.println("COMPLETED WITH NO COLLISION!");
		dis.close();
	}
	private static void checkPresence(byte[] buff, final int C){
		boolean equals=true;
		for(int i=0; i<C; i++){
			for(int j=0; equals && j<C; j++){
				equals=equals && urls[i][j]==buff[j];
			}
			if(equals){
				System.err.println("COLLISION - index: "+C+" and "+ i);
				System.err.println("The repeated buffer is:");
				for(byte b: buff){
					System.out.print(b);
				}
				System.exit(1);
			}
			equals=true;
		}
	}
}
