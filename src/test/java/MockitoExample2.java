import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * Created by borui on 2017/4/25.
 */

//或者使用built-in runner：MockitoJUnitRunner
@RunWith(MockitoJUnitRunner.class) //第2种方式
public class MockitoExample2 {

    @Mock
    private List mockList ;

/*
    //                              第1种明文注解
    public MockitoExample2() {
        MockitoAnnotations.initMocks(this);
    }
*/

    @Test
    public void shorthand() {
        mockList.add(1);
        verify(mockList).add(1);
    }

    @Test(expected = RuntimeException.class)
    public void consecutive_calls() {                            //10、连续调用
        //模拟连续调用返回期望值，如果分开，则只有最后一个有效
        when(mockList.get(0)).thenReturn(0);
        when(mockList.get(0)).thenReturn(1);
        when(mockList.get(0)).thenReturn(2);
        when(mockList.get(1)).thenReturn(0).thenReturn(1).thenThrow(new RuntimeException());

        assertEquals(2,mockList.get(0));
        assertEquals(2,mockList.get(0));
        assertEquals(0,mockList.get(1));
        assertEquals(1,mockList.get(1));

        //第三次或更多调用都会抛出异常
        MockitoExample1.println(mockList.get(1));
    }

    @Test
    public void answer_with_callback() {                        //11、使用回调生成期望值
        //使用Answer来生成我们期望的返回值
        when(mockList.get(anyInt())).thenAnswer(new Answer<Object>() {
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Object []args = invocationOnMock.getArguments() ;
                //线程安全
                //StringBuffer sb = new StringBuffer("Hello World") ;
                //非线程安全
                StringBuilder sb = new StringBuilder("Hello World:") ;
                for (Object arg: args) {
                    sb.append(arg) ;
                }
                return sb.toString();
            }
        });

        assertEquals("Hello World:0",mockList.get(0));
        assertEquals("Hello World:999",mockList.get(998));

    }

    /**
     * 使用spy来监控真实的对象，需要注意的是此时我们需要谨慎的使用when-then语句，而改用do-when语句
     */
    @Test(expected = IndexOutOfBoundsException.class)
    public void spy_on_real_objects() {                         //12、监控真实对象
        List<Integer> list = new LinkedList();
        List spy = spy(list) ;  //监听真实的对象

        when(spy.get(0)).thenReturn(3);

        doReturn(999).when(spy).get(999);

        when(spy.size()).thenReturn(100);

        spy.add(1);
        spy.add(2);
        assertEquals(100,spy.size());
        assertEquals(1,spy.get(0));
        assertEquals(2,spy.get(1));
        verify(spy).add(1);
        verify(spy).add(2);
        assertEquals(999,spy.get(999));
        spy.get(2);
    }

    @Test
    public void unstubbed_invocations() {                       //13、修改对未预设的调用返回默认期望值
        //mock对象使用Answer来对未预设的调用返回默认期望值
        List mock = mock(List.class,new Answer() {
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return 999;
            }
        });

        //下面的get(1)没有预设，通常情况下会返回NULL，但是使用了Answer改变了默认期望值
        assertEquals(999, mock.get(1));

        //下面的size()没有预设，通常情况下会返回0，但是使用了Answer改变了默认期望值
        assertEquals(999,mock.size());
    }

    @Test
    public void capturing_args() {                                  //14、捕获参数来进一步断言
        PersonDao personDao = mock(PersonDao.class);
        PersonService personService = new PersonService(personDao);

        String name = "jack" ;

        ArgumentCaptor<Person> argument = ArgumentCaptor.forClass(Person.class);
        personService.update(1,name);

        verify(personDao).update(argument.capture());

        assertEquals(1,argument.getValue().getId());
        assertEquals(name,argument.getValue().getName());
    }

    class Person {
        private int id;
        private String name;

        Person(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    interface PersonDao {
        public void update(Person person);
    }

    class PersonService {
        private PersonDao personDao;

        PersonService(PersonDao personDao) {
            this.personDao = personDao;
        }

        public void update(int id,String name){
            personDao.update(new Person(id,name));
        }
    }

    @Test
    public void real_partial_mock() {                           //15、真实的部分mock
        //通过spy来调用真实的api
        List list = spy(new ArrayList());
        assertEquals(0,list.size());
        A a  = mock(A.class);

        //通过thenCallRealMethod来调用真实的api
        when(a.doSomething(anyInt())).thenCallRealMethod();
        assertEquals(999,a.doSomething(999));
    }


    class A {
        public int doSomething(int i){
            return i;
        }
    }

    @Test
    public void reset_mock(){                                   //16、重置mock
        List list = mock(List.class);
        when(list.size()).thenReturn(10);
        list.add(1);
        MockitoExample1.println(list.size());
        assertEquals(10,list.size());

        //重置mock，清除所有的互动和预设
        reset(list);
        MockitoExample1.println(list.size());
        assertEquals(0,list.size());


    }





























}
