package com.alexbath.abod3ar;

import android.view.MotionEvent;
import android.view.View;

class UIPlanTreeNodeTouchListener implements View.OnTouchListener {

    private UIPlanTree uiPlanTree;
    private UIPlanTree.Node<ARPlanElement> node;
    private boolean automaticMode = false;
    private float endY;
    private float endX;
    private float dX;
    private float dY;
    private int CLICK_ACTION_THRESHOLD = 25;
    private float startX;
    private float startY;

    public UIPlanTreeNodeTouchListener(UIPlanTree uiPlanTree, UIPlanTree.Node<ARPlanElement> node) {
        this.uiPlanTree = uiPlanTree;
        this.node = node;
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {

        if(automaticMode){
            return true;
        }

        switch (event.getActionMasked()) {

            case MotionEvent.ACTION_DOWN:

                startX = event.getRawX();
                startY = event.getRawY();

//                if(!uiPlanTree.isRoot(node)) {
//                    uiPlanTree.setNodeToDragging(node, true);
//                }

                System.out.println("ACTION_DOWN");
                dX = node.getData().getView().getX() - event.getRawX();
                dY = node.getData().getView().getY() - event.getRawY();

                break;

            case MotionEvent.ACTION_MOVE:
                System.out.println("ACTION_MOVE");

//                if(!uiPlanTree.isRoot(node)) {
//                    uiPlanTree.updateNodeXY(node, event, dX, dY, 0, 0);
//                }

                break;

            case MotionEvent.ACTION_UP:

                endX = event.getRawX();
                endY = event.getRawY();

//                if(!uiPlanTree.isRoot(node)) {
//                    uiPlanTree.setNodeToDragging(node, false);
//                    uiPlanTree.setDragged(node);
//                }

                if (isAClick(startX, endX, startY, endY)) {

                    System.out.println("CLICK!");

                    //uiPlanTree.saveState(node.getParent());

                    uiPlanTree.hideNodes(uiPlanTree.getRoot());

                    uiPlanTree.setFocusedNode(node);



//                    if(node.getChildren().size() >= 3 && !uiPlanTree.isRoot(node)){
//                        uiPlanTree.hideAllOtherDrives(node);
//                    }

//                    uiPlanTree.hideOtherDrivesChildren(node);


//                    for (UIPlanTree.Node<ARPlanElement> arPlanElementNode : node.getChildren()) {
//                        if (arPlanElementNode.getData().getView().getVisibility() == View.INVISIBLE) {
//                            arPlanElementNode.getData().getView().setVisibility(View.VISIBLE);
//                        } else {
//                            uiPlanTree.hideNodes(arPlanElementNode);
//                        }
//                    }

//                    if(uiPlanTree.isRoot(node)){
//                        for (UIPlanTree.Node<ARPlanElement> drive : uiPlanTree.getRoot().getChildren()) {
//                            drive.getData().getView().setVisibility(View.VISIBLE);
//                        }
//                    }
                }

                break;

            default:
                return false;

        }

        return true;
    }

    private boolean isAClick(float startX, float endX, float startY, float endY) {
        float differenceX = Math.abs(startX - endX);
        float differenceY = Math.abs(startY - endY);
        return !(differenceX > CLICK_ACTION_THRESHOLD/* =5 */ || differenceY > CLICK_ACTION_THRESHOLD);
    }

}
