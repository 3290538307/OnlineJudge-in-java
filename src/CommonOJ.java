import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class CommonOJ implements OJ{
	private String file;//���������ļ�
	private int result;
	private long deltaTime;
	private String errorMsg;
	private Map<Integer, String> files;
	private float accuracy;
	private int runMemory,baseMemory;
	private String compile,execute,path,processName;
	public CommonOJ() {
		// TODO Auto-generated constructor stub

	}
	public CommonOJ(String path) {
		this.path=path;
	}
	public int getRunMemory(){
		return runMemory;
	}
	private void setOrders(int language,String subId) {
		switch (language) {
		case 1:   //c++
			compile="g++ "+path+subId+".cpp -o "+path+subId;
			execute=path+subId;
			baseMemory=7152;
			processName=subId;
			break;
		case 2: //c
			compile="gcc "+path+subId+".c -o "+path+subId;
			execute=path+subId;
			baseMemory=7152;
			processName=subId;
			break;
		case 3:
			compile="javac "+path+"Temp.java";
			execute="java -classpath "+path+" Temp";
			baseMemory=13900;
			processName="Temp";
			break;
		case 4:  //python
			compile=null;
			execute="python "+path+subId+".py";
			baseMemory=0;
			processName=subId+".py";
			break;
		default:
			break;
		}
	}
	public void convertCodeToFile(String code,String file) {
		// TODO Auto-generated method stub
		try {
			FileWriter writer=new FileWriter(file);//Ĭ�ϲ�׷������
			writer.write(code);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void setLanguage(int language){
		file=files.get(language);
	}

	public int getResult(){
		return result;
	}
	public long getDeltaTime() {
		return deltaTime;
	}
	@Override
	public String executeCode(String code,Map<Integer, List<String>> data,ExecutorService service,TtlListener ttl,
			MemoryListener 	memOver,int language,String subId) {    //�Ӹ��������Ĺؼ���
		// TODO Auto-generated method stub
		//System.out.println(execute);
		StringBuffer testStates=new StringBuffer("");
		setOrders(language, subId);
		result=def;
		int state=def;   //1 :ac  3: wronganswer 8:CompileError
		Process process=null;
		errorMsg=null;
		accuracy=0;
		runMemory=0;
		try {
			//�������
			if(compile!=null){
				process=Runtime.getRuntime().exec(compile);
				try {
					boolean res=process.waitFor(2000,TimeUnit.MILLISECONDS);                                  
					if(!res) {
						process.destroy();
						errorMsg="���볬ʱ";
						result=state=compileError;
						testStates.append("0");
						//	System.out.println(errorMsg);
					}else {
						errorMsg=getErrors(process.getErrorStream());
						if(errorMsg.indexOf("����")!=-1||errorMsg.indexOf("Error")!=-1||errorMsg.indexOf("error")!=-1||errorMsg.indexOf("undefined")!=-1){//����warning
							result=state=compileError;
							testStates.append("0");
						}
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			deltaTime=0;
			if(state!=compileError){   //����ɹ�
				//�м�����������������͵��ܼ���
				if(data!=null){
					int sum=data.values().size(),correct=0;
					for(List<String> row:data.values()){
						String[] in=row.get(0).split("\\s+");
						String tt=row.get(1);
						String out="";
						int len=0;
						if(!tt.equals("")){
							len=tt.charAt(tt.length()-1)=='\n'?tt.length()-1:tt.length();
							out=row.get(1).substring(0, len).replaceAll("\n", " ").replaceAll("\r", "");
						}
						process=Runtime.getRuntime().exec(execute);//���п�ִ���ļ�
						memOver.setInfo(process, baseMemory, processName, "VmRSS:");
						Future<Boolean> mem=service.submit(memOver);
						//�����������
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						inputData(process.getOutputStream(), in);
						//��ʼ����ʱ��
						//���ó�ʱ�ļ���
						ttl.setProcessAndStarttime(process, System.currentTimeMillis());
						Future<Boolean> res=service.submit(ttl);
						try {
							if(res.get()){//��ȡCallable�ķ��ؽ��
								result=state=timeLimitExceed;
							}
						} catch (InterruptedException | ExecutionException e) {
							e.printStackTrace();
						}
						try {
							if(mem.get()){//��ȡCallable�ķ��ؽ��
								result=state=memoryLimitExceed;
							}
						} catch (InterruptedException | ExecutionException e) {
							e.printStackTrace();
						}
						try {
							process.waitFor();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						deltaTime=ttl.getDeltatime();
						//System.out.println(deltaTime);
						runMemory=memOver.getMemory();
						if(state!=timeLimitExceed&&state!=memoryLimitExceed){
							if(process.exitValue()!=0){
								result=state=runTimeError;
							}
							if(state!=runTimeError){
								String buffer=getOutputData(process.getInputStream());
								if(buffer.equals(out)){
									correct++;
									state=ac;
								}else{
									String tempOut=row.get(1).replaceAll("\\s*", "");
									buffer=buffer.replaceAll("\\s*", "");
									if(buffer.equals(tempOut)){  //��ʽ����
										correct++;
										result=state=presentationError;
									}else{
										result=state=wrongAnswer;
									}
								}
							}
						}
						accuracy=(float)correct/sum;
						testStates.append(state);
						if(state==timeLimitExceed){
							break;
						}
					}
					if(result==def){
						result=ac;
					}
				}	
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return testStates.toString();
	}
	public float getAccuracy() {
		return accuracy;
	}
	private String getErrors(InputStream in) {
		// TODO Auto-generated method stub
		BufferedReader reader=new BufferedReader(new InputStreamReader(in));
		StringBuffer content=new StringBuffer();
		String line="";
		try {
			while((line=reader.readLine())!=null){
				int end=line.lastIndexOf("/");
				if(end!= -1) {
					line=line.substring(end, line.length());
				}
				content.append(line);
			}
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return content.toString();
	}

	@Override
	public void inputData(OutputStream out,String[] data) {
		// TODO Auto-generated method stub
		PrintWriter writer=new PrintWriter(out);
		if(data.length>0){
			//writer.println(data[0]);
			for(int i=0;i<data.length;i++){
				writer.println(data[i]);
			}
		}
		writer.println();
		writer.flush();
		writer.close();
	}

	@Override
	public String getOutputData(InputStream in) {
		// TODO Auto-generated method stub
		BufferedReader reader=new BufferedReader(new InputStreamReader(in));
		String out_=null;
		String outData=new String();
		int i=0;
		try {
			while((out_=reader.readLine())!=null){
				if(i>0){
					outData+=(" "+out_);
				}else{
					outData+=out_;
				}
				i++;
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return outData;
	}
	@Override
	public String getErrors() {
		// TODO Auto-generated method stub
		return errorMsg;
	}
}
