import React from 'react';

type AutoHeightProps = {
  height?: number;
};

function computeHeight(node: HTMLDivElement) {
  const { style } = node;
  style.height = '100%';
  const totalHeight = parseInt(`${getComputedStyle(node).height}`, 10);
  const padding =
    parseInt(`${getComputedStyle(node).paddingTop}`, 10) +
    parseInt(`${getComputedStyle(node).paddingBottom}`, 10);
  return totalHeight - padding;
}

function getAutoHeight(node: HTMLDivElement) {
  let height = computeHeight(node);
  const parentNode = node.parentNode as HTMLDivElement | null;
  if (parentNode) {
    height = computeHeight(parentNode);
  }
  return height;
}

function autoHeight() {
  return <P extends AutoHeightProps>(
    WrappedComponent: React.ComponentClass<P> | React.FC<P>,
  ): React.ComponentClass<P> => {
    class AutoHeightComponent extends React.Component<P & AutoHeightProps> {
      state = {
        computedHeight: 0,
      };

      root: HTMLDivElement | null = null;

      componentDidMount() {
        const { height } = this.props;
        if (!height && this.root) {
          let computedHeight = getAutoHeight(this.root);
          this.setState({ computedHeight });
          if (computedHeight < 1) {
            computedHeight = getAutoHeight(this.root);
            this.setState({ computedHeight });
          }
        }
      }

      handleRoot = (node: HTMLDivElement) => {
        this.root = node;
      };

      render() {
        const { height } = this.props;
        const { computedHeight } = this.state;
        const finalHeight = height || computedHeight;
        return (
          <div ref={this.handleRoot}>
            {finalHeight > 0 && (
              <WrappedComponent {...this.props} height={finalHeight} />
            )}
          </div>
        );
      }
    }

    return AutoHeightComponent;
  };
}

export default autoHeight;
