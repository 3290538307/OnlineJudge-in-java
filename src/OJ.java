import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

public interface OJ {
	//״̬��
	public static final int def=0,ac=1,presentationError=2,wrongAnswer=3,compileError=8,timeLimitExceed=5,runTimeError=4,memoryLimitExceed=6;
	//�Դ�����б��루�ǽ��������ԣ���ִ�У�������ʱ����м��������ز�������״̬������   baseMemory, processName, compareField
	public String executeCode(String code,Map<Integer, List<String>> data,ExecutorService service,TtlListener ttl,
	MemoryListener memOver,int language,String subId);
	//���س���ĸ��ִ�����Ϣ
	public String getErrors();
	//��������������
	public void inputData(OutputStream out,String[] data);
	//��ȡ�ó���ִ����ɺ󣬷��ص�����
	public String getOutputData(InputStream in);
	//���ر�������Ľ����״̬�룩
	public int getResult();
	//���س��������ʱ��
	public long getDeltaTime();
	//���ر������е���ȷ��
	public float getAccuracy();
	//���س���������ڴ�
	public int getRunMemory();
	//�������ԣ�
	public void setLanguage(int language);
	public void convertCodeToFile(String code,String file);
}
