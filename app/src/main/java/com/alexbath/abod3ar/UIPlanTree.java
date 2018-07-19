package com.alexbath.abod3ar;

import android.content.Context;
import android.graphics.Color;
import android.support.constraint.ConstraintLayout;
import android.view.MotionEvent;
import android.view.View;

import com.recklesscoding.abode.core.plan.planelements.action.ActionEvent;
import com.recklesscoding.abode.core.plan.planelements.action.ActionPattern;
import com.recklesscoding.abode.core.plan.planelements.competence.Competence;
import com.recklesscoding.abode.core.plan.planelements.competence.CompetenceElement;
import com.recklesscoding.abode.core.plan.planelements.drives.DriveCollection;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import georegression.struct.point.Point2D_F64;

class UIPlanTree {

    private final List<DriveCollection> driveCollections;
    private final ConstraintLayout overlayLayout;
    public Node<ARPlanElement> root = null;
    public boolean automaticMode = true;
    private Node<ARPlanElement> focusedNode = null;
    private Stack<Node<ARPlanElement>> historyNodes = new Stack<>();
    private int nodeWidthSeperation = 24;
    private int nodeHeightSeperation = 16;
    private boolean showingMore = false;

    public UIPlanTree(List<DriveCollection> driveCollections, Context context, ConstraintLayout overlayLayout) {

        this.driveCollections = driveCollections;
        this.root = new Node<>(new ARPlanElement(context, 0,"Drives", Color.YELLOW));
        this.overlayLayout = overlayLayout;

        createNodes(root,driveCollections,context);
    }

    public Node<ARPlanElement> getRoot() {
        return root;
    }

    public void setUpTreeRender(UIPlanTree.Node<ARPlanElement> node, int widthAppender, int heightAppender, Point2D_F64 viewCenter) {

        if(node.getData().getDragging()){
            return;
        }

        if(node.getParent() == null ){
            node.getData().getView().setX((float) ( viewCenter.x + widthAppender  ));
            node.getData().getView().setY((float) ( viewCenter.y + heightAppender ));

            widthAppender = widthAppender + node.getData().getView().getWidth() + nodeWidthSeperation;

            int childrenTotalHeight = 0;

            for (int i = 0; i < node.getChildren().size(); i++){
                childrenTotalHeight += node.getChildren().get(i).getData().getView().getHeight();
            }

            int heightOffset = 0;

            if(node.getChildren().size() == 1){
                heightOffset = 0;
            }else if(node.getChildren().size() % 2 == 0){
                heightOffset = childrenTotalHeight/2;
            }else if(node.getChildren().size() % 2 != 0){
                heightOffset = childrenTotalHeight/3;
            }

            for (int i = 0; i < node.getChildren().size(); i++){
                setUpTreeRender(node.getChildren().get(i), widthAppender, heightAppender - heightOffset,viewCenter);
                heightAppender = heightAppender + node.getData().getView().getHeight() + nodeHeightSeperation;
            }
        }else{

            if(node.getData().getDragged()){

                stabilizeNode(node);


            }else {

                node.getData().getView().setX((float) (viewCenter.x + widthAppender));
                node.getData().getView().setY((float) (viewCenter.y + heightAppender));

                widthAppender = widthAppender + node.getData().getView().getWidth() + nodeWidthSeperation;

                int childrenTotalHeight = 0;

                for (int i = 0; i < node.getChildren().size(); i++) {
                    childrenTotalHeight += node.getChildren().get(i).getData().getView().getHeight();
                }

                int heightOffset = 0;

                if (node.getChildren().size() == 1) {
                    heightOffset = 0;
                } else if (node.getChildren().size() % 2 == 0) {
                    heightOffset = (int) (childrenTotalHeight / 2.6);
                } else if (node.getChildren().size() % 2 != 0) {
                    heightOffset = childrenTotalHeight / 3;
                }

                for (int i = 0; i < node.getChildren().size(); i++) {
                    setUpTreeRender(node.getChildren().get(i), widthAppender, heightAppender - heightOffset, viewCenter);
                    heightAppender = heightAppender + node.getData().getView().getHeight() + nodeHeightSeperation;
                }
            }
        }

    }

    private void stabilizeNode(UIPlanTree.Node<ARPlanElement> node) {
        node.getData().getView().setX((float) (node.getParent().getData().getView().getX() - node.getData().getNewCoordinates().x));
        node.getData().getView().setY((float) (node.getParent().getData().getView().getY() - node.getData().getNewCoordinates().y));

        for (int i = 0; i < node.getChildren().size(); i++){
            stabilizeNode(node.getChildren().get(i));
        }

    }


    public void removeNodesFromUI(ConstraintLayout rootLayout, Node<ARPlanElement> node) {
        rootLayout.removeView(node.getData().getView());
        node.getChildren().forEach(it -> removeNodesFromUI(rootLayout, it));
    }

    public void hideNodes(Node<ARPlanElement> node) {
        node.getData().getView().setVisibility(View.INVISIBLE);
        node.getChildren().forEach(it -> hideNodes(it));
    }

    public void updateNodesVisuals(String planElementName, Node<ARPlanElement> node) { //focusedNode

        if(node.getData().getName().equals(planElementName)){
            node.getData().setBackgroundColor(Color.parseColor("#0000ff"));

        }

        for (Node<ARPlanElement> child : node.getChildren()){

            if(child.getData().getName().equals(planElementName)){
                child.getData().setBackgroundColor(Color.parseColor("#0000ff"));
            }

        }

        for (Node<ARPlanElement> child : node.getChildren()){

            for (Node<ARPlanElement> grandChild : child.getChildren()){

                if(grandChild.getData().getName().equals(planElementName)){

                    //grandChild.getData().getView().setVisibility(View.VISIBLE);
                    grandChild.getData().setBackgroundColor(Color.parseColor("#0000ff"));

                }else{

                    //grandChild.getData().getView().setVisibility(View.INVISIBLE);

                }

            }

        }

//            node.getData().setBackgroundColor(Color.parseColor("#0000ff"));

//            if(automaticMode) {



//                if (isDrive(node.getParent())) {
//                    hideOtherDrivesChildren(node.getParent());
//                }
//
//                if (isDrive(node) && node.getChildren().isEmpty()) {
//                    hideOtherDrivesChildren(node);
//                }
//
//                if (node.getData().getView().getVisibility() == View.INVISIBLE && isDrive(node.getParent())) {
//                    node.getData().getView().setVisibility(View.VISIBLE);
//                    node.getChildren().forEach(it -> it.getData().getView().setVisibility(View.VISIBLE));
//                }
//            }
//
//        }else{
//            //node.getData().setBackgroundColor(Color.parseColor("#2f4f4f"));
//
//        }

//        node.getChildren().forEach(it -> updateNodesVisuals(planElementName, it));
    }

    public void createNodes(Node<ARPlanElement> node, Object obj, Context context) {

        node.getData().getView().setOnTouchListener(new UIPlanTreeNodeTouchListener(this,node));

        if(obj instanceof ActionEvent){

            Node<ARPlanElement> child = new Node<>(new ARPlanElement(context, 0, ((ActionEvent) obj).getNameOfElement(), Color.MAGENTA));
            node.addChild(child);

        }

        if(obj instanceof ActionPattern){

            Node<ARPlanElement> child = new Node<>(new ARPlanElement(context, 0, ((ActionPattern) obj).getNameOfElement(), Color.GREEN));
            node.addChild(child);

            createNodes(child, ((ActionPattern) obj).getActionEvents(),context);
        }

        if(obj instanceof Competence){

            Node<ARPlanElement> child = new Node<>(new ARPlanElement(context, 0, ((Competence) obj).getNameOfElement(), Color.CYAN));
            node.addChild(child);

            createNodes(child, ((Competence) obj).getCompetenceElements(),context);
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
                        createNodes(child, driveCollection.getTriggeredElement(),context);
                    }else{
                        createNodes(child, null, context);
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
                        createNodes(child, competenceElement.getTriggeredElement(),context);
                    }
                }
            }
        }

    }

    public void hideAllOtherDrives(Node<ARPlanElement> node) {
        if (isDrive(node)) {
            for (Node<ARPlanElement> drive : root.getChildren()) {
                if (!drive.getData().getName().equals(node.getData().getName())) {
                    hideNodes(drive);
                }
            }
        }else{
            hideAllOtherDrives(node.getParent());
        }
    }

    public void hideOtherDrivesChildren(Node<ARPlanElement> node) {
        if (isDrive(node)) {
            for (Node<ARPlanElement> drive : root.getChildren()) {
                if (!drive.getData().getName().equals(node.getData().getName())) {
                    for (Node<ARPlanElement> driveChild : drive.getChildren()) {
                        hideNodes(driveChild);
                    }
                }
            }
        }
    }

    public void updateNodeXY(Node<ARPlanElement> node, MotionEvent event, float dX, float dY, float offsetX, float offsetY) {

        node.getData().setNewCoordinates(new Point2D_F64(node.getParent().getData().getView().getX() - node.getData().getView().getX(), node.getParent().getData().getView().getY() - node.getData().getView().getY()));

        node.getData().getView().setX(event.getRawX() + offsetX + dX);
        node.getData().getView().setY(event.getRawY() + offsetY + dY);

        offsetX += node.getData().getView().getWidth() + 12;

        for (int i = 0; i < node.getChildren().size(); i++) {
            updateNodeXY(node.getChildren().get(i),event,dX,dY,offsetX,offsetY);
            offsetY += node.getData().getView().getHeight() + 12;
        }

    }

    public void setNodeToDragging(Node<ARPlanElement> node, boolean dragging) {
        node.getData().setDragging(dragging);
        node.getChildren().forEach(it -> setNodeToDragging(it,dragging));
    }

    public void setDragged(Node<ARPlanElement> node) {
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

    public boolean getAutomaticMode() {
        return automaticMode;
    }

    public void setAutomaticMode(boolean automaticMode) {
        this.automaticMode = automaticMode;
    }

    public void setDefaultBackgroundColorNodes(Node<ARPlanElement> node) {

        if(node.getData().getView().getVisibility() == View.VISIBLE){
            node.getData().setBackgroundColor(Color.parseColor("#2f4f4f"));
        }

        node.getChildren().forEach(this::setDefaultBackgroundColorNodes);
    }

    public Node<ARPlanElement> getFocusedNode() {
        return focusedNode;
    }

    public void setFocusedNode(Node<ARPlanElement> focusedNode) {

        focusedNode.getData().getView().setVisibility(View.VISIBLE);

        hideNodeParents(focusedNode);
        hideNodeSiblings(focusedNode);
        showNodeChildren(focusedNode);

        //System.out.println("Setting focused node to: "+focusedNode.getData().getUIName());

        this.focusedNode = focusedNode;
    }

    public void setUpTree(int startingXPoint, int startingYPoint, Point2D_F64 viewCenter) {
        this.setUpTreeRender(this.getFocusedNode(),startingXPoint,startingYPoint,viewCenter);
    }

    private void showNodeChildren(Node<ARPlanElement> node) {

        hideNodes(node);

        node.getData().getView().setVisibility(View.VISIBLE);

        for(Node<ARPlanElement> child : node.getChildren()){
            child.getData().getView().setVisibility(View.VISIBLE);
        }

    }


    private void hideNodeSiblings(Node<ARPlanElement> node) {

        if(node.getParent() != null) {
            for (int i = 0; i < node.getParent().getChildren().size(); i++) {
                if(!node.getParent().getChildren().get(i).getData().getName().equals(node.getData().getName())){
                    hideNodes(node.getParent().getChildren().get(i));
                }
            }
        }
    }

    private void hideNodeParents(Node<ARPlanElement> node) {

        if(node.getParent() != null) {
            node.getParent().getData().getView().setVisibility(View.INVISIBLE);
            hideNodeParents(node.getParent());
        }
    }

    public void addNodesToUI(Node<ARPlanElement> node) {

//        if(node.getParent() == null || node.getParent().getParent() == null ){
//            node.getData().getView().setVisibility(View.VISIBLE);
//        }else{
//            //node.getData().getView().setVisibility(View.INVISIBLE);
//        }

        node.getData().getView().setVisibility(View.INVISIBLE);
        overlayLayout.addView(node.getData().getView());

        node.getChildren().forEach(it -> addNodesToUI(it));
    }

    public void addNodeToUI(Node<ARPlanElement> node){
        overlayLayout.addView(node.getData().getView());
    }

    public void renderPreviousState() {

        if(!historyNodes.empty()){
            setFocusedNode(historyNodes.pop());
        }

        renderGrandChildren(getFocusedNode());

    }

    public void initState() {

        addNodesToUI(root);

        if(historyNodes.empty()) { //first time

            setFocusedNode(root);

            root.getData().getView().setVisibility(View.VISIBLE);

            for (Node<ARPlanElement> child : root.getChildren()) {
                child.getData().getView().setVisibility(View.VISIBLE);

            }

        }

    }

    public void saveState(Node<ARPlanElement> node) {

        if(historyNodes.empty()){
            System.out.println("saving state!");
            historyNodes.push(node);
        }

        if(!historyNodes.peek().getData().getName().equals(getFocusedNode().getData().getName())) {
            System.out.println("saving state!");
            historyNodes.push(node);
        }else{
            System.out.println("not saving state!");
        }
    }

    public boolean isFocusedNode(Node<ARPlanElement> node) {
        return focusedNode.getData().getName().equals(node.getData().getName());
    }

    public void renderGrandChildren(Node<ARPlanElement> node) {

        if(showingMore) {

            for (Node<ARPlanElement> child : node.getChildren()) {
                for (Node<ARPlanElement> grandChild : child.getChildren()) {
                    grandChild.getData().getView().setVisibility(View.VISIBLE);
                }
            }

        }else{

            for (Node<ARPlanElement> child : node.getChildren()) {
                for (Node<ARPlanElement> grandChild : child.getChildren()) {
                    grandChild.getData().getView().setVisibility(View.INVISIBLE);
                }
            }
        }
    }

    public void setShowingMore(boolean showingMore) {
        this.showingMore = showingMore;
    }

    public boolean getShowingMore() {
        return showingMore;
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
