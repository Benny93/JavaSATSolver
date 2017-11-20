package dataStructure;

/**
 * Created by Benny on 04.02.17.
 */
public class CDCLEx10 extends CDCL {

    private int[] selectionOrder;
    private int selectionIndex;
    /**
     * Constructor
     *
     * @param instance Clause Set instance the CDCL works on
     */
    public CDCLEx10(ClauseSet instance, int[] selectionOrder) {
        super(instance);
        this.selectionOrder = selectionOrder;
        this.selectionIndex = 0;
    }

    @Override
    public boolean solve() {
        this.selectionIndex = 0;
        printSelectionOrder();
        return super.solve();
    }

    @Override
    protected Variable getNextVar() {
        if (selectionIndex < selectionOrder.length && this.instance.getVariables().containsKey(selectionOrder[selectionIndex])){
            Variable nextVar = this.instance.getVariables().get(selectionOrder[selectionIndex]);
            selectionIndex++;
            if(!this.stack.contains(nextVar)) {
                return nextVar;
            }else {
                return getNextVar();
            }
        }
        return null;
    }

    private void printSelectionOrder(){
        String printable = "Selection Order: { ";
        for(int i = 0; i < selectionOrder.length; i++){
            printable += selectionOrder[i] + " ";
        }
        printable += "}";
        System.out.println(printable);
    }


}
