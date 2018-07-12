package com.alexbath.abod3ar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.support.constraint.ConstraintLayout;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.recklesscoding.abode.core.plan.planelements.PlanElement;
import com.recklesscoding.abode.core.plan.planelements.action.ActionEvent;
import com.recklesscoding.abode.core.plan.planelements.action.ActionPattern;
import com.recklesscoding.abode.core.plan.planelements.competence.Competence;
import com.recklesscoding.abode.core.plan.planelements.competence.CompetenceElement;
import com.recklesscoding.abode.core.plan.planelements.drives.DriveCollection;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import georegression.struct.point.Point2D_F64;

class UIPlanTree {

    public Node<ARPlanElement> root = null;

    public UIPlanTree(List<DriveCollection> driveCollections, Context context) {

        root = new Node<>(new ARPlanElement(context, 0,"Drives", Color.YELLOW));

//        for (int i = 0; i < driveCollections.size(); i++) {
//
//            int drivePriority = i+1;
//
//            DriveCollection driveCollection = driveCollections.get(i);
//            Node<ARPlanElement> child = root.addChild(new Node<>(new ARPlanElement(context, drivePriority , driveCollection.getNameOfElement(), Color.RED)));
//
//            PlanElement triggeredElement = driveCollection.getTriggeredElement();
//
//            if(triggeredElement != null){
//
//                child.addChild(new Node<>(new ARPlanElement(context, 0, triggeredElement.getNameOfElement() , Color.GREEN)));
//                //quick hack to set level 2 elements to invisible
//                child.getChildren().get(0).getData().getView().setVisibility(View.INVISIBLE);
//
//                child.getData().getView().setOnClickListener(v -> {
//
//                    if(child.getChildren().get(0).getData().getView().getVisibility() == View.VISIBLE){
//                        child.getChildren().get(0).getData().getView().setVisibility(View.INVISIBLE);
//                    }else{
//                        child.getChildren().get(0).getData().getView().setVisibility(View.VISIBLE);
//                    }
//
//                });
//            }
//        }
    }

    public Node<ARPlanElement> getRoot() {
        return root;
    }

    public static void printTree(Node<ARPlanElement> node, String appender){
        System.out.println(appender + node.getData().getUIName());
        node.getChildren().forEach(it -> printTree(it,appender + appender));
    }

    public void addNodesToUI(ConstraintLayout rootLayout, Node<ARPlanElement> node) {

        if(node.getParent() == null || node.getParent().getParent() == null ){
            node.getData().getView().setVisibility(View.VISIBLE);
        }else{
            node.getData().getView().setVisibility(View.VISIBLE);
        }

        rootLayout.addView(node.getData().getView());
        node.getChildren().forEach(it -> addNodesToUI(rootLayout,it));
    }

    public void removeNodesFromUI(ConstraintLayout rootLayout, Node<ARPlanElement> node) {
        rootLayout.removeView(node.getData().getView());
        node.getChildren().forEach(it -> removeNodesFromUI(rootLayout,it));
    }

    public void hideNode(Node<ARPlanElement> node) {
        node.getData().getView().setVisibility(View.INVISIBLE);
        node.getChildren().forEach(it -> hideNode(it));
    }

    public void setNodeBackgroundColor(String planElementName, Node<ARPlanElement> node) {

        if(node.getData().getName().equals(planElementName)){
            node.getData().setBackgroundColor(Color.parseColor("#0000ff"));
        }else{
            node.getData().setBackgroundColor(Color.parseColor("#2f4f4f"));
        }

        node.getChildren().forEach(it -> setNodeBackgroundColor(planElementName, it));
    }

    public void addNodes(Node<ARPlanElement> node, Object obj, Context context) {

        node.getData().getView().setOnTouchListener(new View.OnTouchListener() {

            float dX;
            float dY;
            int lastAction;

            public boolean onTouch(View view, MotionEvent event) {

                switch (event.getActionMasked()) {

                    case MotionEvent.ACTION_DOWN:

                        setNodeToDragging(node,true);

                        System.out.println("ACTION_DOWN");
                        dX = node.getData().getView().getX() - event.getRawX();
                        dY = node.getData().getView().getY() - event.getRawY();
                        lastAction = MotionEvent.ACTION_DOWN;
                        break;

                    case MotionEvent.ACTION_MOVE:
                        System.out.println("ACTION_MOVE");

                        updateNodeXYRecursion(node,event,dX,dY,0,0);

//                        node.getData().getView().setX(event.getRawX() + dX);
//                        node.getData().getView().setY(event.getRawY() + dY);
//
//                        node.getChildren().get(0).getData().getView().setX(event.getRawX() + node.getData().getView().getWidth() + 12 + dX);
//                        node.getChildren().get(0).getData().getView().setY(event.getRawY()  + dY);

                        //node.getData().setNewCoordinates(new Point2D_F64(dX, dY));

                        lastAction = MotionEvent.ACTION_MOVE;
                        break;

                    case MotionEvent.ACTION_UP:

//                        if (isDrive(node)) {
//                            for (Node<ARPlanElement> drive : root.getChildren()) {
//                                if (!drive.getData().getName().equals(node.getData().getName())) {
//                                    for (Node<ARPlanElement> driveChild : drive.getChildren()) {
//                                        hideNode(driveChild);
//                                    }
//                                }
//                            }
//                        }
//
//                        for (Node<ARPlanElement> arPlanElementNode : node.getChildren()) {
//                            if (arPlanElementNode.getData().getView().getVisibility() == View.INVISIBLE) {
//                                arPlanElementNode.getData().getView().setVisibility(View.VISIBLE);
//                            } else {
//                                hideNode(arPlanElementNode);
//                            }
//                        }

                        setNodeToDragging(node,false);

                        setDragged(node);

                        System.out.println("ACTION_UP");
                        if (lastAction == MotionEvent.ACTION_DOWN)
                            System.out.println("ACTION_UP inside");
                        break;

                    default:
                        return false;

                }

                return true;
            }
        });

        if(obj instanceof ActionEvent){

            Node<ARPlanElement> child = new Node<>(new ARPlanElement(context, 0, ((ActionEvent) obj).getNameOfElement(), Color.MAGENTA));
            node.addChild(child);

        }

        if(obj instanceof ActionPattern){

            Node<ARPlanElement> child = new Node<>(new ARPlanElement(context, 0, ((ActionPattern) obj).getNameOfElement(), Color.GREEN));
            node.addChild(child);

            addNodes(child, ((ActionPattern) obj).getActionEvents(),context);
        }

        if(obj instanceof Competence){

            Node<ARPlanElement> child = new Node<>(new ARPlanElement(context, 0, ((Competence) obj).getNameOfElement(), Color.CYAN));
            node.addChild(child);

            addNodes(child, ((Competence) obj).getCompetenceElements(),context);
        }

        if(obj == null){
            return;
        }

        if (obj instanceof LinkedList) {

            for (int i = 0; i < ((LinkedList) obj).size(); i++) {

                if(((LinkedList) obj).get(i) instanceof DriveCollection){

                    DriveCollection driveCollection = (DriveCollection) ((LinkedList) obj).get(i);
                    Node<ARPlanElement> child = new Node<>(new ARPlanElement(context, i + 1, driveCollection.getNameOfElement(), Color.RED));
                    node.addChild(child);

                    if(driveCollection.getTriggeredElement() != null) {
                        addNodes(child, driveCollection.getTriggeredElement(),context);
                    }
                }

                if(((LinkedList) obj).get(i) instanceof ActionEvent){

                    ActionEvent actionEvent = (ActionEvent) ((LinkedList) obj).get(i);
                    Node<ARPlanElement> child = new Node<>(new ARPlanElement(context, 0, actionEvent.getNameOfElement(), Color.YELLOW));
                    node.addChild(child);

                }

                if(((LinkedList) obj).get(i) instanceof CompetenceElement){

                    CompetenceElement competenceElement = (CompetenceElement) ((LinkedList) obj).get(i);
                    Node<ARPlanElement> child = new Node<>(new ARPlanElement(context, 0, competenceElement.getNameOfElement(), Color.BLACK));
                    node.addChild(child);

                    if(competenceElement.getTriggeredElement() != null) {
                        addNodes(child, competenceElement.getTriggeredElement(),context);
                    }
                }
            }
        }

    }

    private void updateNodeXYRecursion(Node<ARPlanElement> node, MotionEvent event, float dX, float dY, float offsetX, float offsetY) {

        node.getData().getView().setX(event.getRawX() + offsetX + dX);
        node.getData().getView().setY(event.getRawY() + offsetY + dY);

        offsetX += node.getData().getView().getWidth() + 12;

        for (int i = 0; i < node.getChildren().size(); i++) {
            updateNodeXYRecursion(node.getChildren().get(i),event,dX,dY,offsetX,offsetY);
            offsetY += node.getData().getView().getHeight() + 12;
        }

    }

    private void setNodeToDragging(Node<ARPlanElement> node, boolean dragging) {
        node.getData().setDragging(dragging);
        node.getChildren().forEach(it -> setNodeToDragging(it,dragging));
    }

    private void setDragged(Node<ARPlanElement> node) {
        node.getData().setDragged(true);
        node.getChildren().forEach(it -> setDragged(it));
    }

    public boolean isRoot(Node<ARPlanElement> node) {

        if(node.getParent() == null){
            return true;
        }

        return false;

    }

    public boolean isDrive(Node<ARPlanElement> node) {

        if(node.getParent() == null){
            return false;
        }

        if(node.getParent().getParent() == null){
            return true;
        }

        return false;
    }


    public class Node<T>{

        private T data = null;
        private List<Node<T>> children = new ArrayList<>();
        private Node<T> parent = null;

        public Node(T data){
            this.data = data;
        }

        public Node<T> addChild(Node<T> child){
            child.setParent(this);
            this.children.add(child);
            return child;
        }

        public List<Node<T>> getChildren() {
            return children;
        }

        public T getData(){
            return data;
        }

        public void setData(T Data){
            this.data = data;
        }

        private void setParent(Node<T> parent){
            this.parent = parent;
        }

        public Node<T> getParent(){
            return parent;
        }

    }
}
