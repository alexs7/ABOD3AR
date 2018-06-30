package com.alexbath.abod3ar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.constraint.ConstraintLayout;

import com.recklesscoding.abode.core.plan.planelements.PlanElement;
import com.recklesscoding.abode.core.plan.planelements.drives.DriveCollection;

import java.util.ArrayList;
import java.util.List;

import georegression.struct.point.Point2D_F64;

class UIPlanTree {

    public Node<ARPlanElement> root = null;

    public UIPlanTree(List<DriveCollection> driveCollections, Context context) {

        root = new Node<>(new ARPlanElement(context, "Drives", Color.YELLOW));

        for (int i = 0; i < driveCollections.size(); i++) {

            DriveCollection driveCollection = driveCollections.get(i);
            Node<ARPlanElement> child = root.addChild(new Node<>(new ARPlanElement(context, driveCollection.getNameOfElement(), Color.RED)));

            PlanElement triggeredElement = driveCollection.getTriggeredElement();
            if(triggeredElement != null){
                child.addChild(new Node<>(new ARPlanElement(context, triggeredElement.getNameOfElement() , Color.GREEN)));
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

    public void renderPlan(Canvas canvas, Node<ARPlanElement> node, Point2D_F64 center, boolean highLevel, Paint paint) {

        root.getData().getView().setX((float) (center.x - root.getData().getView().getWidth()/2));
        root.getData().getView().setY((float) (center.y - root.getData().getView().getHeight()/2));

        for (int i = 0; i < root.getChildren().size(); i++) {
            //TODO: 4 should be drivesList.size()!
            float xV = (float) (center.x + 290 * Math.cos(Math.PI / root.getChildren().size() * (2*i + 1)));
            float yV = (float) (center.y + 290 * Math.sin(Math.PI / root.getChildren().size() * (2*i + 1)));

            root.getChildren().get(i).getData().getView().setX(Math.round(xV));
            root.getChildren().get(i).getData().getView().setY(Math.round(yV));

            drawLine(canvas,
                    new Point2D_F64(center.x,center.y),
                    new Point2D_F64(
                            xV+ root.getChildren().get(i).getData().getView().getWidth()/2,
                            yV + root.getChildren().get(i).getData().getView().getHeight()/2),
                    paint);

            if(highLevel){

                for (int j = 0; j < root.getChildren().get(i).getChildren().size(); j++) {

                    float xVchild = (float) (root.getChildren().get(i).getData().getView().getX() - (root.getChildren().get(i).getChildren().get(j).getData().getView().getWidth() - root.getChildren().get(i).getData().getView().getWidth())/2); //+ 100 * Math.cos(Math.PI / root.getChildren().get(i).getChildren().size() * (2*j + 1)));
                    float yVchild = (float) (root.getChildren().get(i).getData().getView().getY() + root.getChildren().get(i).getChildren().get(j).getData().getView().getHeight() - root.getChildren().get(i).getData().getView().getHeight()/3);//(root.getChildren().get(i).getChildren().get(j).getData().getView().getY() + 100 * Math.sin(Math.PI / root.getChildren().get(i).getChildren().size() * (2*j + 1)));

                    root.getChildren().get(i).getChildren().get(j).getData().getView().setX(xVchild);
                    root.getChildren().get(i).getChildren().get(j).getData().getView().setY(yVchild);
                }
            }

        }
    }

    private void drawLine( Canvas canvas , Point2D_F64 a , Point2D_F64 b , Paint color ) {
        canvas.drawLine((float)a.x,(float)a.y,(float)b.x,(float)b.y,color);
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
