package pl.netigen.core.rewards;

import java.util.ArrayList;
import java.util.List;

public class RewardListenersList extends ArrayList<RewardsListener> {

    public void callOnFail() {
        for (RewardsListener listener : this) {
            if (listener != null)
                listener.onFail();
        }
    }

    public void callOnSuccess(List<RewardItem> rewardedItems) {
        for (RewardsListener listener : this) {
            if (listener != null)
                listener.onSuccess(rewardedItems);
        }
    }

    @Override
    public boolean add(RewardsListener rewardsListener) {
        if (!this.contains(rewardsListener))
            return super.add(rewardsListener);
        else return false;
    }
}
