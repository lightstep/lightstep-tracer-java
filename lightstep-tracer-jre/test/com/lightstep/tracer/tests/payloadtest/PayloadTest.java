package io.traceguide.tests.PayloadTest;

import io.traceguide.instrument.*;
import io.traceguide.instrument.runtime.*;
import java.util.*;

public class PayloadTest{
    public static void main(String[] args) {
        // Initialize runtime
        io.traceguide.instrument.Runtime runtime = io.traceguide.instrument.runtime.JavaRuntime.getInstance();
        boolean success = runtime.initialize("localhost", 9998, "DEVELOPMENT_TOKEN_sahil", "dev_sahil");  

        if (success) {
            ActiveSpan span = runtime.span("Logs with payloads sent");
            
            runtime.log("Logging with a null payload", null);
            runtime.log("Logging with a char payload", 'a');
            runtime.log("Logging with a boolean payload", true);
            runtime.log("Logging with an integer payload", -324234234);
            runtime.log("Logging with an float payload", 3.2);
            runtime.log("Logging with an double payload", 3.13123);
            runtime.log("Logigng with a string literal payload", "Payload String Literal Test");

            String payloadString = "Payload String Test";
            runtime.log("Logging with a string payload", payloadString);

            DummyObject obj = new DummyObject(5600);
            runtime.log("DummyObject payload", obj);

            CyclicObject1 obj1 = new CyclicObject1();
            CyclicObject2 obj2 = new CyclicObject2(obj1);
            runtime.log("CyclicObject payload", obj1);
   
            span.end();
        }
    }

    public static class DummyObject {
        public Object objPayload = null;
        public char charPayload = 'a';
        public boolean booleanPayload = true;
        public int intPayload = -324234234;
        public double doublePayload = 3.13123;
        public String stringPayload = "Payload String Test";
    
        public String [] stringArrayPayload = {"item1", "item2"};
        public TreeMap<String, Integer> treeMapPayload;

        public DummyObject(int num) {
            treeMapPayload = new TreeMap<String, Integer>();
            for (int i = 0; i < num; i++) {
                treeMapPayload.put("Key" + i, i);
            }
        }
    }


    public static class CyclicObject1 {
        public String message = "This CyclicObject1";
        public CyclicObject2 obj;

        public CyclicObject1 (){
            obj = new CyclicObject2(this);
        } 
    }

    public static class CyclicObject2 {
        public String message = "This CyclicObject2";
        public CyclicObject1 obj;

        public CyclicObject2 (CyclicObject1 obj) {
            this.obj = obj;
        }
    }    
}