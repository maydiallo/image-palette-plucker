import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.imageio.ImageIO;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;


/**
 * Connects and computes commands
 * 
 * @author Maimouna Diallo
 * @version PX
 */

public class Client extends GridPane{
		//declaring variables
		private ImageView imgTop; 
		private ImageView imgFinal;
		private ImageView imgGray;
		private ImageView imgCanny;
		private Button btnColour; 
		private Button btnCrop;
		private Button btnSelect;
		private Button btnGray;
		private Button btnCanny;
		private TextField txtColour1;
     	private TextField txtColour2;
     	private TextField txtColour3;
     	private TextField txtColour4;
     	private TextField txtColour5;
     	private TextField txtColour6;
     	private  Socket s;
		private ArrayList<String> arrColours= new ArrayList<String>();
		static ArrayList<String> tempVal = new ArrayList<String>();
 		static ArrayList<Integer> tempAmt = new ArrayList<Integer>();
 		static ArrayList<String> arrAmt = new ArrayList<String>();
		private TextArea txtArea;
		private DataOutputStream dos;
		private BufferedOutputStream bos;
		private InputStream is;
		private OutputStream os;
		private BufferedReader br;
		boolean running = false;
		private String cropURL = "/api/Crop";
		private String grayURL = "/api/GrayScale";
		private String cannyURL = "/api/Canny" ;
		private Image crop;
		private Image gray;
		private Image canny;
		private PrintWriter pw;
		private FileChooser fc;
		private File imagec;
		private File newimg;
		private File imgG;
		private File imgC;
		private String fName;
		private BufferedImage img;
		private String newFname;
		private String newGray;
		private String newCanny;
		private String filepath;
	
	public Client(Stage stage) {
		//Sets up GUI
		setUI();
		
		btnSelect.setOnAction(e-> { 
			//Select Image using file chooser
			fc = new FileChooser();
			imagec = fc.showOpenDialog(stage); 
			if (imagec!=null)
			{ 
				fName= imagec.getName();
	    		System.out.println(fName);
	    		filepath= imagec.getAbsolutePath();
	    		Image readIMG = new Image("file:"+filepath);
	    		imgTop.setImage(readIMG);
			}else 
			{
				 txtArea.appendText("You have not selected a file\r\n");
			}
    		
    		
        } );
		
		
		btnCanny.setOnAction(e-> {  
			//Connect to server first
			Connect();
    		String encodedFile=null;
    		try {
    			
    			//Take Image that has been converted to grayscale and use canny edge detection
    			File image= new File(imgG.getAbsolutePath());
				FileInputStream fis = new FileInputStream(image);
				byte[] bytes = new byte [(int)image.length()];
				fis.read(bytes);
				encodedFile= new String (Base64.getEncoder().encodeToString(bytes));
				byte[] bytesSend = encodedFile.getBytes();
				
				
				 dos.write(("POST "+cannyURL+ " HTTP/1.1\r\n").getBytes());
				 dos.write(("Content-Type: "+"application/text\r\n").getBytes());
				 dos.write(("Content-Length: "+ encodedFile.length()+"\r\n").getBytes());
				 dos.write(("\r\n").getBytes());
				 dos.write(bytesSend);
				 dos.flush();
				 dos.write(("\r\n").getBytes());
				 
				 txtArea.appendText("POST command sent\r\n");
				 
				
				 String response = "";
				 String line = "";
				 
				 while(!(line=br.readLine()).equals(""))
				 {
					 response += line +"\n";
				 }
				 System.out.println(response);
				 
				 String imgData="";
				 
				 while((line=br.readLine())!=null)
				 {
					 imgData += line;
				 }
				 
				 System.out.println(imgData);
				 
				 String base64str=imgData.substring(imgData.indexOf('\'')+1, imgData.lastIndexOf('}')-1); 
				 System.out.println(base64str);
				 
				 byte[] decodeStr = Base64.getDecoder().decode(base64str);
				 canny = new Image(new ByteArrayInputStream(decodeStr)); 
				 img = ImageIO.read(new ByteArrayInputStream(decodeStr));
				 
				 //Depending on the file extension save image appropriately 
				 if(fName.endsWith(".jpg"))
				 {
					 newCanny= fName.replace(".jpg", "canny.jpg");
					 
				 }else  if(fName.endsWith(".jpeg"))
				 {
					 newCanny= fName.replace(".jpeg", "canny.jpeg");
				 }else  if(fName.endsWith(".png"))
				 {
					 newCanny= fName.replace(".png", "canny.png");
				 }
				 imgC= new File("data/"+newCanny);
				 ImageIO.write(img, "jpg",imgC);
				
				
				//Display the canny image
				 imgCanny.setImage(canny);
				 imgCanny.setFitHeight(200);
				 imgCanny.setFitWidth(200);
				 
				
		    		
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
    		
            
        } );
		
		
		btnColour.setOnAction(e-> { 
			//Find the mazimum values
			findMax();
			//Display the colour and RGB Value
			txtColour1.setText(arrAmt.get(0));
			txtColour1.setStyle("-fx-background-color: rgb("+arrAmt.get(0)+");");
			
			txtColour2.setText(arrAmt.get(1));
			txtColour2.setStyle("-fx-background-color: rgb("+arrAmt.get(1)+");");
			
			
			txtColour3.setText(arrAmt.get(2));
			txtColour3.setStyle("-fx-background-color: rgb("+arrAmt.get(2)+");");
			
			txtColour4.setText(arrAmt.get(3));
			txtColour4.setStyle("-fx-background-color: rgb("+arrAmt.get(3)+");");
			
			txtColour5.setText(arrAmt.get(4));
			txtColour5.setStyle("-fx-background-color: rgb("+arrAmt.get(4)+");");
			
			txtColour6.setText(arrAmt.get(5));
			txtColour6.setStyle("-fx-background-color: rgb("+arrAmt.get(5)+");");
	    	
          
        } );
		
		btnGray.setOnAction(e-> { 
			//Connect to server
			Connect();
			String encodedFile=null;
    		try {
    			
    			//Use selected file to convert to grayscale
    			File image= new File(filepath);
				FileInputStream fis = new FileInputStream(image);
				byte[] bytes = new byte [(int)image.length()];
				fis.read(bytes);
				encodedFile= new String (Base64.getEncoder().encodeToString(bytes));
				byte[] bytesSend = encodedFile.getBytes();
				
				
				 dos.write(("POST "+grayURL+ " HTTP/1.1\r\n").getBytes());
				 dos.write(("Content-Type: "+"application/text\r\n").getBytes());
				 dos.write(("Content-Length: "+ encodedFile.length()+"\r\n").getBytes());
				 dos.write(("\r\n").getBytes());
				 dos.write(bytesSend);
				 dos.flush();
				 dos.write(("\r\n").getBytes());
				 
				 txtArea.appendText("POST command sent\r\n");
				 
				
				 String response = "";
				 String line = "";
				 
				 while(!(line=br.readLine()).equals(""))
				 {
					 response += line +"\n";
				 }
				 System.out.println(response);
				 
				 String imgData="";
				 
				 while((line=br.readLine())!=null)
				 {
					 imgData += line;
				 }
				 
				 System.out.println(imgData);
				 
				 String base64str=imgData.substring(imgData.indexOf('\'')+1, imgData.lastIndexOf('}')-1); 
				 System.out.println(base64str);
				 
				 byte[] decodeStr = Base64.getDecoder().decode(base64str);
				 gray = new Image(new ByteArrayInputStream(decodeStr)); 
				 img = ImageIO.read(new ByteArrayInputStream(decodeStr));
				 
				//Depending on the file extension save image appropriately 
				 if(fName.endsWith(".jpg"))
				 {
					 newGray= fName.replace(".jpg", "gray.jpg");
					 
				 }else  if(fName.endsWith(".jpeg"))
				 {
					 newGray= fName.replace(".jpeg", "gray.jpeg");
				 }else  if(fName.endsWith(".png"))
				 {
					 newGray= fName.replace(".png", "gray.png");
				 }
				
				 imgG= new File("data/"+newGray);
				 ImageIO.write(img, "jpg",imgG);
				
				 imgGray.setImage(gray);
				 imgGray.setFitHeight(200);
				 imgGray.setFitWidth(200);
    		
    		} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
            
    		} );
		
		
		btnCrop.setOnAction(e-> {  
			//Connect to server
			Connect();
			//Use selected file to crop the image
    		String encodedFile=null;
    		try {
    			
    			File image= new File(filepath);
				FileInputStream fis = new FileInputStream(image);
				byte[] bytes = new byte [(int)image.length()];
				fis.read(bytes);
				encodedFile= new String (Base64.getEncoder().encodeToString(bytes));
				byte[] bytesSend = encodedFile.getBytes();
				
				
				 dos.write(("POST "+cropURL+ " HTTP/1.1\r\n").getBytes());
				 dos.write(("Content-Type: "+"application/text\r\n").getBytes());
				 dos.write(("Content-Length: "+ encodedFile.length()+"\r\n").getBytes());
				 dos.write(("\r\n").getBytes());
				 dos.write(bytesSend);
				 dos.flush();
				 dos.write(("\r\n").getBytes());
				 
				 txtArea.appendText("POST command sent\r\n");
				 
				
				 String response = "";
				 String line = "";
				 
				 while(!(line=br.readLine()).equals(""))
				 {
					 response += line +"\n";
				 }
				 System.out.println(response);
				 
				 String imgData="";
				 
				 while((line=br.readLine())!=null)
				 {
					 imgData += line;
				 }
				 
				 System.out.println(imgData);
				 
				 String base64str=imgData.substring(imgData.indexOf('\'')+1, imgData.lastIndexOf('}')-1); 
				 System.out.println(base64str);
				 
				 byte[] decodeStr = Base64.getDecoder().decode(base64str);
				 crop = new Image(new ByteArrayInputStream(decodeStr)); 
				 img = ImageIO.read(new ByteArrayInputStream(decodeStr));
				 
				//Depending on the file extension save image appropriately 
				 if(fName.endsWith(".jpg"))
				 {
					 newFname= fName.replace(".jpg", "crop.jpg");
					 
				 }else  if(fName.endsWith(".jpeg"))
				 {
					 newFname= fName.replace(".jpeg", "crop.jpeg");
				 }else  if(fName.endsWith(".png"))
				 {
					 newFname= fName.replace(".png", "crop.png");
				 }
				 newimg= new File("data/"+newFname);
				 ImageIO.write(img, "jpg",newimg);
				
				
				
				 imgFinal.setImage(crop);
				 imgFinal.setFitHeight(200);
				 imgFinal.setFitWidth(200);
				 
				 findcolours();
				 countFrequencies(arrColours);
		    		
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
    		
            
        } );
		
		
		
	}
	
	//Set up user interface
	 private void setUI()
		{
		 
	    	setHgap(5);
	    	setVgap(5);
	    	
	    	setAlignment(Pos.CENTER);
	    	imgTop= new ImageView();
	    	imgTop.setFitHeight(200);
	    	imgTop.setFitWidth(200);
	    	btnCrop= new Button("Crop");
	    	btnColour= new Button("Colour");
	    	btnSelect= new Button("Select Image");
	    	btnGray = new Button("GrayScale");
	    	btnCanny=new Button("Canny");
	    	txtColour1 = new TextField();
	     	txtColour2 = new TextField();
	     	txtColour3 = new TextField();
	     	txtColour4 = new TextField();
	     	txtColour5 = new TextField();
	     	txtColour6 = new TextField();
	    	txtArea= new TextArea();
	    	txtArea.setPrefHeight(50);
	    	imgFinal = new ImageView();
	    	imgGray= new ImageView();
	    	imgCanny= new ImageView();
	    	
	    	
	    	
	    	add(btnSelect,0,0);
	    	add(imgTop,1,0);
	    	add(btnGray,0,2);
	    	add(btnCanny,1,2);
	    	add(btnCrop,0,1);
	    	add(btnColour,1,1);
	    	
	    	
	    	add(txtArea,0,3,2,1);
	    	add(txtColour1,0,4);
	    	add(txtColour2,0,5);
	    	add(txtColour3,0,6);
	    	add(txtColour4,0,7);
	    	add(txtColour5,0,8);
	    	add(txtColour6,0,9);
	    	add(imgFinal,0,10);
	    	add(imgGray,1,10);
	    	add(imgCanny,2,10);
	    	
	    	
	    	
	    	
			 
	    	
			
		}
	 
	 //Find the colours of the image in RGB
	 private void findcolours()
	 {
		 arrColours.clear();
		  BufferedImage bufferedImage;
		try {
			bufferedImage = ImageIO.read(new File("data",newFname));
			int height = bufferedImage.getHeight(), width = bufferedImage.getWidth();
			 for (int y = 0; y < height; y++) {
		            for (int x = 0; x < width; x++) {
		                int RGBA = bufferedImage.getRGB(x, y);
		                int alpha = (RGBA >> 24) & 255;
		                int red = (RGBA >> 16) & 255;
		                int green = (RGBA >> 8) & 255;
		                int blue = RGBA & 255;
		                
		                String rgbval= red +", "+ green +", "+blue;
		                //Add values found to array 
		                arrColours.add(rgbval);
		            }
		        }
	        //txtColours=new TextField[count];
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	        
	 }
	 
	 
	 //Determine how many times each colour appears
	 private void countFrequencies(ArrayList<String> list)
	    {
		 tempVal.clear();
		 tempAmt.clear();
	        Set<String> st = new HashSet<String>(list);
	        for (String s : st)
	        {
	        	
	        	int amt= Collections.frequency(list, s);
	        	//add the frequencies and rgb value into arrays
	            tempVal.add(s);
	            tempAmt.add(amt);
	        }
	      
	        
	        
	    }
	 
	 
	 //Find the maximum frequencies
	 private void findMax()
	    {
		 		
		 arrAmt.clear();
	    
	     		int times = 6;
	            while(times>0)
	            {
	            	if(tempAmt.size()==0)
	            	{	            	
	            		 txtArea.appendText("You have viewed all the colours\r\n");
	            	}else {
	            		int max = Collections.max(tempAmt);
		            	
	 	 	           int index = tempAmt.indexOf(max);
	 	 	           String maxVal = tempVal.get(index);
	 	 	           arrAmt.add(maxVal);
	 	 	           //After the max has been found remove the value drom the array to find the next one
	 	 	           tempAmt.remove(index);
	 	 	           tempVal.remove(index);
	 	 	           times-=1;
	            	}
	            	
	            }
	    }
	 
	
	 //Connect to the server 
	 private void Connect()
	    {
		 		
		 try {
			 //Binding of streams
				s= new Socket("localhost", 5000);
				is = s.getInputStream();
				br=new BufferedReader(new InputStreamReader(is));
				os= s.getOutputStream();
				bos = new BufferedOutputStream(os);
				dos = new DataOutputStream(bos);
				pw = new PrintWriter(os, true);
				
				
				txtArea.appendText("Connected to server \r\n");
			} catch (UnknownHostException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
 		
		   
	    }
	 
	
	

}
