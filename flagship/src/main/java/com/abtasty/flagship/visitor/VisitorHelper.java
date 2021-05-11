//package com.abtasty.flagship.visitor;
//
//public class VisitorHelper {
//
//    VisitorDelegate visitor;
//
//    public VisitorHelper(Visitor visitor) {
//        this.visitor = new VisitorDelegate(visitor);
//    }
//
//    public VisitorDelegate visitor {
//        return visitor;
//    }
//}
//
//
////public class VisitorHelper implements IVisitor {
////
////    VisitorDelegate visitor;
////
////    public VisitorHelper(Visitor visitor) {
////        this.visitor = new VisitorDelegate(visitor);
////    }
////
////    @Override
////    public void updateContext(HashMap<String, Object> context) {
////        this.visitor.updateContext(context);
////    }
////
////    @Override
////    public <T> void updateContext(String key, T value) {
////        this.visitor.updateContext(key, value);
////    }
////
////    @Override
////    public CompletableFuture<Visitor> synchronizeModifications() {
////        return this.visitor.synchronizeModifications();
////    }
////
////    @Override
////    public <T> T getModification(String key, T defaultValue) {
////        return this.visitor.getModification(key, defaultValue);
////    }
////
////    @Override
////    public <T> T getModification(String key, T defaultValue, boolean activate) {
////        return this.visitor.getModification(key, defaultValue, activate);
////    }
////
////    @Override
////    public JSONObject getModificationInfo(String key) {
////        return this.visitor.getModificationInfo(key);
////    }
////
////    @Override
////    public void activateModification(String key) {
////        this.visitor.activateModification(key);
////    }
////
////    @Override
////    public <T> void sendHit(Hit<T> hit) {
////        this.visitor.sendHit(hit);
////    }
////}
