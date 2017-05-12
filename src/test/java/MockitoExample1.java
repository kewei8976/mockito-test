import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.exceptions.verification.NoInteractionsWanted;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

/**
 * Created by borui on 2017/4/25.
 */
public class MockitoExample1 {

    @Test
    public void verify_behaviour() {                    //1、验证行为
        //mock create a list
        List mock = mock(List.class);
        //assert  mock.size() != 0:"不为空";
        assertEquals("不等",mock.size(),0);
        assertNotNull(mock);

        mock.add(1);
        println(mock.size());
        mock.clear();

        verify(mock).add(1);
        println(mock.get(0));
        verify(mock).clear();
    }

    @Test
    public void when_thenReturn() {                     //2、模拟我们所期望的结果
        Iterator iterator = mock(Iterator.class);

        //预设当iterator调用next()时第一次返回hello，第n次都返回world
        when(iterator.next()).thenReturn("hello").thenReturn("world").thenReturn("!");

        //使用mock的对象
        String result = iterator.next() + " " + iterator.next() + " " + iterator.next();
        println(result);
        assertEquals("hello world !",result);
    }

    @Test
    public void stubbing_test() {
        LinkedList mockedList = mock(LinkedList.class);

        when(mockedList.get(0)).thenReturn("the first");
        when(mockedList.get(1)).thenThrow(new RuntimeException());

        println(mockedList.get(0));

        // following throws runtime exception
        println(mockedList.get(1));

        println(mockedList.get(999));

        verify(mockedList).get(0);
    }

    /**
     * 需要注意的是如果你使用了参数匹配，那么所有的参数都必须通过matchers来匹配
     */
    @Test
    public void with_arguments() {                          //3、参数匹配
        Comparable comparable = mock(Comparable.class);

        int ok = 1 , err = 2;

        //预设根据不同的参数返回不同的结果
        when(comparable.compareTo("Test")).thenReturn(ok);
        when(comparable.compareTo("Omg")).thenReturn(ok);
        assertEquals(ok,comparable.compareTo("Test"));
        assertEquals(ok,comparable.compareTo("Omg"));

        //对于没有预设的情况会返回默认值
        assertEquals(0, comparable.compareTo("Not stub"));
    }

    @Test
    public void with_unspecified_arguments() { //除了匹配制定参数外，还可以匹配自己想要的任意参数
        List list =  mock(List.class);

        int args = anyInt();
        println(args);

        //匹配参数
        when(list.get(args)).thenReturn(1);
        when(list.contains(argThat(new IsValid()))).thenReturn(true);
        assertEquals(1, list.get(1));
        assertEquals(1, list.get(999));
        assertTrue(list.contains(1)) ;
        assertTrue(!list.contains(3));
    }

    private class IsValid extends ArgumentMatcher<List>{
        @Override
        public boolean matches(Object o) {
            return  o.equals(1) || o.equals(2);
        }
    }

    @Test
    public void all_arguments_provided_by_matchers() {
        Comparator comparator = mock(Comparator.class);
        comparator.compare("nihao","hello");

        //如果你使用了参数匹配，那么所有的参数都必须通过matchers来匹配
        verify(comparator).compare(anyString(),eq("hello"));

        //下面的为无效的参数匹配使用
        verify(comparator).compare(anyString(),"hello");
    }

    @Test
    public void verifying_number_of_invocations() {             //4、验证确切的调用次数
        List<Integer> list = mock(List.class);
        list.add(1);
        list.add(2);
        list.add(2);
        list.add(3);
        list.add(3);
        list.add(3);
/*

        for (int i:list) {
            verify(list, atMost(1)).add(i);
        }

*/
        //验证是否被调用一次，等效于下面的times(1)
        verify(list).add(1);
        verify(list, times(1)).add(1);

        //验证是否被调用2次
        verify(list, times(2)).add(2);

        //验证是否被调用3次
        verify(list, times(3)).add(3);

        //验证是否从未被调用过
        verify(list, never()).add(4);

        //验证至少调用一次
        verify(list, atLeastOnce()).add(1);

        //验证至少调用2次
        verify(list, atLeast(2)).add(2);

        //验证至多调用3次
        verify(list, atMost(3)).add(3);
    }

    @Test(expected = RuntimeException.class)
    public void doThrow_when() {                            //5、模拟方法体抛出异常
        List list = mock(List.class);
        doThrow(new RuntimeException()).when(list).add(1);

        list.add(1);
    }

    @Test(expected = IOException.class)
    public void when_thenThrow() throws IOException {
        OutputStream ops = mock(OutputStream.class);
        OutputStreamWriter writer = new OutputStreamWriter(ops);

        //预设当流关闭时抛出异常
        doThrow(new IOException()).when(ops).close();
        ops.close();

    }

    @Test
    public void verification_in_order() {                   //6、验证执行顺序
        List list = mock(List.class);
        List list2 = mock(List.class);
        list.add(1);
        list2.add("hello");
        list.add(2);
        list2.add("world");

        //将需要排序的mock对象放入InOrder
        InOrder inOrder = inOrder(list,list2);

        //下面的代码不能颠倒顺序，验证执行顺序
        inOrder.verify(list).add(1);
        inOrder.verify(list2).add("hello");
        inOrder.verify(list).add(2);
        inOrder.verify(list2).add("world");
    }

    @Test
    public void verify_interaction() {                      //7、确保模拟对象上无互动发生
        List list = mock(List.class);
        List list2 = mock(List.class);
        List list3 = mock(List.class);
        list.add(1);
        verify(list).add(1);
        verify(list,never()).add(2);

        //验证零互动行为
        verifyZeroInteractions(list2,list3);
    }

    @Test(expected = NoInteractionsWanted.class)
    public void find_redundant_interaction() {              //8、找出冗余的互动(即未被验证到的)
        List list = mock(List.class);
        list.add(1);
        list.add(2);
        verify(list,times(2)).add(anyInt());

        //检查是否有未被验证的互动行为，因为add(1)和add(2)都会被上面的anyInt()验证到，所以下面的代码会通过
        verifyNoMoreInteractions(list);

        List list2 = mock(List.class);
        list2.add(1);
        list2.add(2);
        verify(list2).add(1);

        //检查是否有未被验证的互动行为，因为add(2)没有被验证，所以下面的代码会失败抛出异常
        verifyNoMoreInteractions(list2);
    }

    /**
     *                                                      //9、使用注解来快速模拟
     * 在上面的测试中我们在每个测试方法里都mock了一个List对象，为了避免重复的mock，是测试类更具有可读性，我们可以使用下面的注解方式来快速模拟对象：
     * 正确方式参考MockitoExample2
     */
    @Mock
    private List testMockList ;

    @Test
    public void shorthand() { // 运行这个测试类你会发现报错了，mock的对象为NULL，为此我们必须在基类中添加初始化mock的代码
        testMockList.add(1);
        verify(testMockList).add(1);
    }

    static void println(Object val) {
        System.out.println("--------------------");
        System.out.println(val);
        System.out.println("--------------------");
    }

}
