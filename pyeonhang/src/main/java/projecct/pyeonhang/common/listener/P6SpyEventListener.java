package projecct.pyeonhang.common.listener;

import com.p6spy.engine.common.ConnectionInformation;
import com.p6spy.engine.event.JdbcEventListener;
import com.p6spy.engine.spy.P6SpyOptions;
import projecct.pyeonhang.common.config.P6sypSqlFormater;

import java.sql.SQLException;

public class P6SpyEventListener extends JdbcEventListener {
    @Override
    public void onAfterConnectionClose(ConnectionInformation connectionInformation, SQLException e) {
        P6SpyOptions.getActiveInstance().setLogMessageFormat(P6sypSqlFormater.class.getName());
    }
}
