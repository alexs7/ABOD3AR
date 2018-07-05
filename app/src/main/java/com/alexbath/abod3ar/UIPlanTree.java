package com.alexbath.abod3ar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.support.constraint.ConstraintLayout;
import android.view.View;

import com.recklesscoding.abode.core.plan.planelements.PlanElement;
import com.recklesscoding.abode.core.plan.planelements.drives.DriveCollection;

import java.util.ArrayList;
import java.util.List;

import georegression.struct.point.Point2D_F64;

class UIPlanTree {

    public Node<ARPlanElement> root = null;
    final float pts[] = new float[2];

    public UIPlanTree(List<DriveCollection> driveCollections, Context context) {

        root = new Node<>(new ARPlanElement(context, 0,"Drives", Color.YELLOW));

        for (int i = 0; i < driveCollections.size(); i++) {

            int drivePriority = i+1;

            DriveCollection driveCollection = driveCollections.get(i);
            Node<ARPlanElement> child = root.addChild(new Node<>(new ARPlanElement(context, drivePriority , driveCollection.getNameOfElement(), Color.RED)));

            PlanElement triggeredElement = driveCollection.getTriggeredElement();

            if(triggeredElement != null){

                child.addChild(new Node<>(new ARPlanElement(context, 0, triggeredElement.getNameOfElement() , Color.GREEN)));
                //quick hack to set level 2 elements to invisible
                child.getChildren().get(0).getData().getView().setVisibility(View.INVISIBLE);

                child.getData().getView().setOnClickListener(v -> {

                    if(child.getChildren().get(0).getData().getView().getVisibility() == View.VISIBLE){
                        child.getChildren().get(0).getData().getView().setVisibility(View.INVISIBLE);
                    }else{
                        child.getChildren().get(0).getData().getView().setVisibility(View.VISIBLE);
                    }

                });
            }
        }
    }

    public Node<ARPlanElement> getRoot() {
        return root;
    }

    public static void printTree(Node<ARPlanElement> node, String appender){
        System.out.println(appender + node.getData().getUIName());
        node.getChildren().forEach(it -> printTree(it,appender + appender));
    }

    public void addNodesToUI(ConstraintLayout rootLayout, Node<ARPlanElement> node) {
        rootLayout.addView(node.getData().getView());
        node.getChildren().forEach(it -> addNodesToUI(rootLayout,it));
    }

    public void setNodeBackgroundColor(String planElementName, Node<ARPlanElement> node) {

        if(node.getData().getName().equals(planElementName)){
            node.getData().setBackgroundColor(Color.parseColor("#0000ff"));
        }else{
            node.getData().setBackgroundColor(Color.parseColor("#2f4f4f"));
        }

        node.getChildren().forEach(it -> setNodeBackgroundColor(planElementName, it));
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
