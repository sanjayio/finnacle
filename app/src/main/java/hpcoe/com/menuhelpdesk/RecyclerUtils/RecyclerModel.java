package hpcoe.com.menuhelpdesk.RecyclerUtils;

/**
 * Created by Abhijith Gururaj and Sanjay Kumar.
 *
 * This is an abstraction of the data which is to be displayed on each viewGroup in the
 * RecyclerView.
 *
 * @see RecyclerAdapter.RecyclerViewHolder where this data is set on the TextViews.
 */
public class RecyclerModel {


    private final String mText;
    private final String mShortDesc;

    /**
     * Constructor to set the data.
     * @param text : Menu Option
     * @param mShortDesc : Short Description
     */
    public RecyclerModel(String text, String mShortDesc) {

        mText = text;
        this.mShortDesc=mShortDesc;
    }


    /**
     * Getter for Menu option.
     * @return : String containing Menu option
     */
    public String getText() {
        return mText;
    }

    /**
     * Getter for Short Description
     * @return : String containing Short Description.
     */
    public String getShortDesc(){
        return mShortDesc;
    }
}
