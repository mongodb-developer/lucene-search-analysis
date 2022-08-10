package com.mongodb.atlas.search.analysis.views.about;

import com.mongodb.atlas.search.analysis.views.MainLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.svg.Svg;
import com.vaadin.flow.component.svg.elements.Path;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("About")
@Route(value = "about", layout = MainLayout.class)
public class AboutView extends VerticalLayout {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AboutView() {
        setSpacing(false);

        Image img = new Image("images/atlas-search.png", "MongoDB Atlas Search");
        img.setWidth("200px");
        add(img);

        add(new H2("MongoDB Atlas Search Analysis"));
        add(new Paragraph("Built for Ethan Steininger 😁"));
        
        Svg gitIcon = new Svg();
        gitIcon.viewbox(0, 0, 24, 24);
        Path path = new Path("path",
            "M12 .3a12 12 0 0 0-3.8 23.4c.6.1.8-.3.8-.6v-2c-3.3.7-4-1.6-4-1.6-.6-1.4-1.4-1.8-1.4-1.8-1-.7.1-.7.1-.7 1.2 0 1.9 1.2 1.9 1.2 1 1.8 2.8 1.3 3.5 1 0-.8.4-1.3.7-1.6-2.7-.3-5.5-1.3-5.5-6 0-1.2.5-2.3 1.3-3.1-.2-.4-.6-1.6 0-3.2 0 0 1-.3 3.4 1.2a11.5 11.5 0 0 1 6 0c2.3-1.5 3.3-1.2 3.3-1.2.6 1.6.2 2.8 0 3.2.9.8 1.3 1.9 1.3 3.2 0 4.6-2.8 5.6-5.5 5.9.5.4.9 1 .9 2.2v3.3c0 .3.1.7.8.6A12 12 0 0 0 12 .3");
        gitIcon.add(path);
        gitIcon.setWidth("100%");
        gitIcon.setHeight("32px");
        Div outerDiv = new Div();
        Div iconDiv = new Div();
        iconDiv.add(gitIcon);
        Div textDiv = new Div();
        Anchor a = new Anchor(
        		"https://github.com/10gen/lucene-search-analysis",
        		"Check this out on GitHub");
        textDiv.add(a);
        outerDiv.add(iconDiv, textDiv);
        add(outerDiv);
        /*
         * <svg class="MuiSvgIcon-root MuiSvgIcon-colorPrimary" focusable="false" viewBox="0 0 24 24" aria-hidden="true">
         * <path d="></path></svg>
         * </div>
         * <div class="MuiGrid-root MuiGrid-item">
         * 		<a class="MuiTypography-root MuiLink-root MuiLink-underlineHover MuiTypography-colorPrimary" href="https://github.com/10gen/presales-mside" target="_blank" rel="noopener">
         * 			<p class="MuiTypography-root MuiTypography-body1 MuiTypography-colorPrimary">Check this out on GitHub</p>
         * 		</a>
         * </div>
         * </div>
         */

        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        getStyle().set("text-align", "center");
    }

}
