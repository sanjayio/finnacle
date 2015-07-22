package hpcoe.com.menuhelpdesk.RecyclerUtils;

import hpcoe.com.menuhelpdesk.R;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by: Abhijith Gururaj and Sanjay Kumar
 *
 * This is an Adapter for the Recycler view which is used to display the meu options.
 * This adapter uses a ViewHolder to bind the data to the ViewHolders.
 * Overridden methods: onCreateViewHolder, onBindViewHolder and getItemCount.
 *
 * An item click listener is also implemented for this adapter.This listener enables us
 * to start a new activity at the main thread.
 *
 * @see hpcoe.com.menuhelpdesk.MenuOptions where the RecyclerAdapter is used.
 */
public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.RecyclerViewHolder> {


    private final LayoutInflater mInflater;
    private final List<RecyclerModel> mModels;
    private Context context;
    ItemClickListener onItemClickListener;
    private int touchedPosition=-1;

    /**
     * Declare the ItemClickListener
     */
    public interface ItemClickListener{
        public void onItemClick(View view,int position);
    }

    /**
     * Constructor for initializing the data for the Recyclerview
     * @param context : The context from which the Adapter is called.
     * @param models : A list of Menu options(data) and their short Description.
     */
    public RecyclerAdapter(Context context, List<RecyclerModel> models) {
        this.context=context;
        mInflater = LayoutInflater.from(context);
        mModels = new ArrayList<>(models);

    }

    /**
     * @param parent : The view group is the base class for layouts and views
     * containers. This is an abstraction will contain data
     *               (Menuoption and its corresponding Short Description).
     *
     * @param viewType :
     * @return : A ViewHolder(view which will display data)
     */
    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
         View itemView = mInflater.inflate(R.layout.list_item, parent, false);

        return new RecyclerViewHolder(itemView);
    }

    /**
     * This method binds the views to the corresponding data in the list.
     * @param holder : ViewHolder(view) in which the data is to be displayed.
     * @param position : The position(int) of the view in the list.
     */
    @Override
    public void onBindViewHolder(RecyclerViewHolder holder, final int position) {
        final RecyclerModel model = mModels.get(position);
        holder.bind(model);
    }

    /**
     * This returns the size of the list containing the data.
     * @return : Size of the items in the List.(int)
     */
    @Override
    public int getItemCount() {
        return mModels.size();
    }

    /**
     * Method for initializing the adapter's onitemClickListener to the one which is
     * defined in the Main thread. The user can define the work to be done in the listener
     * at main thread and assign it to the adapter's Listener.
     * @param listener : NotNull. Custom ItemClickListener defined by the user
     */

    public void setItemClickListener(final ItemClickListener listener){
        this.onItemClickListener=listener;
    }


    /**
     * A ViewHolder(view) which is used by the adapter to display data.
     * @see RecyclerModel where the data for this view is fetched.
     */
    public class RecyclerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {


        private final TextView tvText;
        private final TextView shortDesc;

        /**
         * Constructor to instantiate the two TextViews
         * @param itemView : The ViewGroup containing the two TextViews.
         *                 (refer list_item.xml)
         */
        public RecyclerViewHolder(View itemView) {
            super(itemView);

            tvText = (TextView) itemView.findViewById(R.id.txt_listitem);
            shortDesc= (TextView) itemView.findViewById(R.id.txt_listitemShort);
            itemView.setOnClickListener(this);
        }

        /**
         * Getter for Menu option TextView
         * @return : A string containing corresponding MenuOption
         */
        public String getTvText(){
            return tvText.getText().toString();
        }

        /**
         * Setter for the two TextViews.
         * @param model : The abstraction(class) containing the data.
         */
        public void bind(RecyclerModel model) {
            tvText.setText(model.getText());
            shortDesc.setText(model.getShortDesc());
        }

        /**
         * Implementing an onClick Listener for the view. When the user clicks on the
         * view, this will in turn call the Adapter's OnItemClickListener which is defined
         * by the user.
         * @param v : The view where the user has clicked.
         */
        @Override
        public void onClick(View v) {
            Log.d("Item Clicked: ",getTvText()+" at "+getAdapterPosition());
            // newSelectedPos=getAdapterPosition();
            //notifyItemChanged(newSelectedPos);
            if(onItemClickListener!=null)
                onItemClickListener.onItemClick(v,getAdapterPosition());
        }

    }

    /**
     * Implementing the Search Filter Functionality.
     * Whenever the list is changed, This method calls the necessary methods for
     * addition and removal of views in the RecyclerView.
     * @param models : List of data.
     */
    public void animateTo(List<RecyclerModel> models) {
        applyAndAnimateRemovals(models);
        applyAndAnimateAdditions(models);
        applyAndAnimateMovedItems(models);
    }


    /**
     * Removing the views in the List.
     * @param newModels : List of data
     */
    private void applyAndAnimateRemovals(List<RecyclerModel> newModels) {
        for (int i = mModels.size() - 1; i >= 0; i--) {
            final RecyclerModel model = mModels.get(i);
            if (!newModels.contains(model)) {
                removeItem(i);
            }
        }
    }

    /**
     * Adding the views in the list.
     * @param newModels : List of data
     */
    private void applyAndAnimateAdditions(List<RecyclerModel> newModels) {
        for (int i = 0, count = newModels.size(); i < count; i++) {
            final RecyclerModel model = newModels.get(i);
            if (!mModels.contains(model)) {
                addItem(i, model);
            }
        }
    }

    /**
     * Moving the item Views from its current Position to the
     * actual position where it should be
     * @param newModels : List of data
     */
    private void applyAndAnimateMovedItems(List<RecyclerModel> newModels) {
        for (int toPosition = newModels.size() - 1; toPosition >= 0; toPosition--) {
             RecyclerModel model = newModels.get(toPosition);
             int fromPosition = mModels.indexOf(model);
            if (fromPosition >= 0 && fromPosition != toPosition) {
                moveItem(fromPosition, toPosition);
            }
        }
    }

    /**
     * Removal of item.
     * @param position : The position of the item which is to be removed
     */
    public void removeItem(int position) {
        mModels.remove(position);
        notifyItemRemoved(position);

    }

    /**
     * Adding the item in the list.
     * @param position: The position of the item which has to be added to the list.
     * @param model: The abstraction(object of class) to be added.
     */
    public void addItem(int position, RecyclerModel model) {
        mModels.add(position, model);
        notifyItemInserted(position);
    }


    /**
     * Method to add the item in List and Moving the item to the required position.
     * @param fromPosition: The initial position of the view
     * @param toPosition : The final position of the view.
     */
    public void moveItem(int fromPosition, int toPosition) {
        final RecyclerModel model = mModels.remove(fromPosition);
        mModels.add(toPosition, model);
        notifyItemMoved(fromPosition, toPosition);
    }
}
