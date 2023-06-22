package org.eclipse.uprotocol;

public interface UTest<T> {

    T getMyData();
    void setMyData(T myData);

    public class MyStringImplementation implements UTest<String> {
        String myData;
        public String getMyData() {
            return myData;
        }
        public void setMyData(String myData) {
            this.myData = myData;
        }
    }

    public class MyIntegerImplementation implements UTest<Integer> {
        Integer myData;
        public Integer getMyData() {
            return myData;
        }
        public void setMyData(Integer myData) {
            this.myData = myData;
        }
    }

}