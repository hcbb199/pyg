package cn.neteast.solrUtil;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:spring/applicationContext*.xml")
public class ImportItemList {
    @Autowired
    private SolrUtil solrUtil;

    @Test
    public void importList() {
        solrUtil.importItemData();
    }
    @Test
    public void deleteList() {
        solrUtil.deleteItem();
    }
}
